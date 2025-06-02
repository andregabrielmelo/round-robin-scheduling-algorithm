package simulator.roundrobin.scheduler;

import lombok.Data;
import simulator.roundrobin.enums.BurstType;
import simulator.roundrobin.enums.ProcessState;
import simulator.roundrobin.model.Burst;
import simulator.roundrobin.model.CpuCore;
import simulator.roundrobin.model.CpuProcess;
import simulator.roundrobin.process.QuantumStrategy;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class RoundRobinScheduler {
	private final List<CpuProcess> allProcesses;
	private final List<CpuCore> cores;
	private final QuantumStrategy quantumStrategy;

	private final Queue<CpuProcess> readyQueue = new ArrayDeque<>();
	private final Map<CpuProcess, Integer> blocked = new HashMap<>();

	private final List<StringBuilder> timeline = new ArrayList<>();

	private int globalTime = 0;
	private int busyTicks = 0;

	public RoundRobinScheduler(List<CpuProcess> processes, int coreCount, QuantumStrategy quantumStrategy) {
		this.allProcesses = processes;
		this.quantumStrategy = quantumStrategy;
		this.cores = new ArrayList<>(coreCount);
		for (int i = 0; i < coreCount; i++) {
			cores.add(CpuCore.builder().runningProcess(null).quantumRemaining(0).build());
			timeline.add(new StringBuilder());
		}
	}

	public void run() {
		while (!simulationFinished()) {

			/* 1. novos processos que chegam */
			allProcesses.stream()
					.filter(p -> p.getArrivalTime() == globalTime && p.getState() == ProcessState.NEW)
					.forEach(p -> {
						p.setState(ProcessState.READY);
						readyQueue.add(p);
					});

			/* 2. desbloqueios de I/O */
			List<CpuProcess> finishedIo = blocked.entrySet().stream()
					.filter(e -> e.getValue() <= 0)
					.map(Map.Entry::getKey)
					.collect(Collectors.toList());

			for (CpuProcess p : finishedIo) {
				blocked.remove(p);

				/* avançamos UM passo para o próximo burst após o I/O */
				p.setCurrentBurstIndex(p.getCurrentBurstIndex() + 1);

				if (p.hasMoreBursts()) {
					p.setState(ProcessState.READY);
					readyQueue.add(p);
				} else {
					p.setState(ProcessState.DONE);
					p.setTurnaroundTime(globalTime - p.getArrivalTime());
				}
			}

			/* 3. tick nos bursts de I/O restantes */
			blocked.replaceAll((proc, left) -> left - 1);

			/* 4. acumulamos tempo de espera */
			readyQueue.forEach(proc -> proc.setWaitingTime(proc.getWaitingTime() + 1));

			/* 5. avanço de cada core */
			for (int idx = 0; idx < cores.size(); idx++) {
				CpuCore core = cores.get(idx);

				if (!core.isIdle()) {
					busyTicks++;

					CpuProcess cur = core.getRunningProcess();
					core.setQuantumRemaining(core.getQuantumRemaining() - 1);
					cur.setRemainingBurstTime(cur.getRemainingBurstTime() - 1);

					if (cur.getRemainingBurstTime() == 0) {
						cur.setCurrentBurstIndex(cur.getCurrentBurstIndex() + 1);

						if (cur.hasMoreBursts()) {
							Burst next = cur.currentBurst();
							if (next.getType() == BurstType.IO) {
								/* envia para I/O, sem pular o burst */
								cur.setState(ProcessState.BLOCKED);
								blocked.put(cur, next.getDuration());
							} else {
								/* próximo burst é CPU, só será tratado quando voltar à fila */
							}
						} else {
							cur.setState(ProcessState.DONE);
							cur.setTurnaroundTime(globalTime + 1 - cur.getArrivalTime());
						}
						core.setRunningProcess(null);
					}

					if (!core.isIdle() && core.getQuantumRemaining() == 0) {
						cur.setState(ProcessState.READY);
						readyQueue.add(cur);
						cur.setContextSwitches(cur.getContextSwitches() + 1);
						core.setRunningProcess(null);
					}
				}

				if (core.isIdle() && !readyQueue.isEmpty()) {
					CpuProcess next = readyQueue.poll();
					if (next.getRemainingBurstTime() == 0) {
						Burst burst = next.currentBurst();
						next.setRemainingBurstTime(burst.getDuration());
					}
					next.setState(ProcessState.RUNNING);
					core.setRunningProcess(next);
					core.setQuantumRemaining(quantumStrategy.quantum(next));
				}

				timeline.get(idx).append(core.isIdle() ? '-' : core.getRunningProcess().getId());
			}

			globalTime++;
		}
	}

	private boolean simulationFinished() {
		return allProcesses.stream().allMatch(p -> p.getState() == ProcessState.DONE);
	}

	public void printMetrics() {
		System.out.println("\n===== Métricas =====");
		System.out.printf("%-6s | %-10s | %-11s | %-12s%n", "Proc", "Espera", "Turnaround", "TrocasCtx");
		System.out.println("-----------------------------------------------");
		allProcesses.forEach(p ->
				System.out.printf("%-6s | %-10d | %-11d | %-12d%n", p.getId(), p.getWaitingTime(), p.getTurnaroundTime(), p.getContextSwitches()));

		double cpuUtil = 100.0 * busyTicks / (globalTime * cores.size());
		System.out.println("\nUtilização total da CPU: " + String.format(Locale.US, "%.2f", cpuUtil) + "%");
		System.out.println("Tempo total simulado: " + globalTime + " ticks\n");
	}

	public void printTimeline() {
		System.out.println("===== Linha do tempo (Gantt) =====");
		for (int i = 0; i < timeline.size(); i++) {
			System.out.println("Core " + i + ": " + timeline.get(i));
		}
	}

	public List<String> timelineStrings() {
		return timeline.stream().map(StringBuilder::toString).collect(Collectors.toList());
	}
}
