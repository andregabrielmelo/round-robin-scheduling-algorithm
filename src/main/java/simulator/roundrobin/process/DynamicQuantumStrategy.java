package simulator.roundrobin.process;

import simulator.roundrobin.enums.BurstType;
import simulator.roundrobin.model.Burst;
import simulator.roundrobin.model.CpuProcess;

public class DynamicQuantumStrategy implements QuantumStrategy {
	private final int defaultQuantum;

	public DynamicQuantumStrategy(int defaultQuantum) {
		this.defaultQuantum = defaultQuantum;
	}

	public int quantum(CpuProcess p) {
		Burst current = p.currentBurst();
		if (current.getType() == BurstType.CPU) {
			return Math.min(defaultQuantum, Math.max(1, current.getDuration() / 2));
		}
		return defaultQuantum; // fallback
	}
}
