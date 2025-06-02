package simulator.roundrobin.process;

import simulator.roundrobin.model.CpuProcess;

public class FixedQuantumStrategy implements QuantumStrategy {
	private final int quantum;

	public FixedQuantumStrategy(int quantum) {
		if (quantum <= 0) throw new IllegalArgumentException("Quantum must be > 0");
		this.quantum = quantum;
	}

	@Override
	public int quantum(CpuProcess p) {
		return quantum;
	}
}