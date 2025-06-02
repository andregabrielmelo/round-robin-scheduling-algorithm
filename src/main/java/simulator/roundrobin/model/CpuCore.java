package simulator.roundrobin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CpuCore {
	private CpuProcess runningProcess;
	private int quantumRemaining;

	public boolean isIdle() {
		return runningProcess == null;
	}
}