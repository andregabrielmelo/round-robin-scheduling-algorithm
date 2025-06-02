package simulator.roundrobin.report;

import org.jfree.chart.*;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.gantt.*;
import org.jfree.data.time.SimpleTimePeriod;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Gera um gráfico Gantt onde cada tick da timeline equivale
 * a 1 segundo (1 u.t.). O eixo X exibe apenas o valor em segundos.
 */
public class ChartGenerator {

	/* converte tick -> Date (epoch + tick * 1000 ms) */
	private static Date toDate(int tick) {
		return new Date(tick * 1_000L);
	}

	/**
	 * @param timelines  linhas por núcleo (ex.: "AABBC--D")
	 * @param outPng     caminho do arquivo PNG
	 */
	public static void generateGantt(List<String> timelines, String outPng)
			throws IOException {

		TaskSeriesCollection dataset = new TaskSeriesCollection();
		for (int core = 0; core < timelines.size(); core++) {
			String line = timelines.get(core);
			TaskSeries serie = new TaskSeries("Core " + core);

			int start = 0;
			char prev = line.charAt(0);
			int seg = 0;

			for (int t = 1; t < line.length(); t++) {
				char cur = line.charAt(t);
				if (cur != prev) {
					if (prev != '-') {
						serie.add(new Task(prev + "#" + (++seg),
								new SimpleTimePeriod(toDate(start), toDate(t))));
					}
					start = t;
					prev = cur;
				}
			}
			if (prev != '-') {
				serie.add(new Task(prev + "#" + (++seg),
						new SimpleTimePeriod(toDate(start), toDate(line.length()))));
			}

			dataset.add(serie);
		}

		JFreeChart chart = ChartFactory.createGanttChart(
				"Round-Robin – Linha do Tempo",
				"Núcleo", "Tempo (u.t.)", dataset,
				true, false, false);

		CategoryPlot plot = chart.getCategoryPlot();
		((GanttRenderer) plot.getRenderer()).setShadowVisible(false);

		DateAxis axis = (DateAxis) plot.getRangeAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("s"));        // mostra só segundos
		axis.setTickUnit(new DateTickUnit(DateTickUnitType.SECOND, 1));
		axis.setVerticalTickLabels(false);
		axis.setLabel("Tempo (u.t.)");

		int width  = Math.max(600, timelines.get(0).length() * 15);
		int height = 120 + timelines.size() * 60;
		ChartUtils.saveChartAsPNG(new File(outPng), chart, width, height);

		System.out.println("Gantt salvo em: " + outPng);
	}
}