package simulator.roundrobin.model;

import lombok.*;
import simulator.roundrobin.enums.ProcessState;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CpuProcess {
	private String id;
	private int arrivalTime;
	private List<Burst> bursts;

	@Builder.Default
	private int currentBurstIndex = 0;
	@Builder.Default
	private int remainingBurstTime = 0;
	@Builder.Default
	private ProcessState state = ProcessState.NEW;

	/* métricas */
	@Builder.Default
	private int waitingTime = 0;
	@Builder.Default
	private int turnaroundTime = 0;
	@Builder.Default
	private int contextSwitches = 0;
	@Builder.Default
	private float cpuUse = 0;

	public Burst currentBurst() {
		if (!hasMoreBursts()) {
			throw new IllegalStateException("Processo " + id + " não possui mais bursts.");
		}
		return bursts.get(currentBurstIndex);
	}

	public boolean hasMoreBursts() {
		return currentBurstIndex < bursts.size();
	}
}