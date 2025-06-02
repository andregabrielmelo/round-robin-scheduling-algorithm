package simulator.roundrobin.process;

import simulator.roundrobin.model.CpuProcess;

public interface QuantumStrategy {
	int quantum(CpuProcess p);
}
