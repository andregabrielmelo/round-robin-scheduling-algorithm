package simulator.roundrobin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import simulator.roundrobin.enums.BurstType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Burst {
	private BurstType type;
	private int duration; // in ticks
}