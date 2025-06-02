package simulator.roundrobin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import simulator.roundrobin.model.CpuProcess;
import simulator.roundrobin.process.DynamicQuantumStrategy;
import simulator.roundrobin.process.FixedQuantumStrategy;
import simulator.roundrobin.report.ChartGenerator;
import simulator.roundrobin.scheduler.RoundRobinScheduler;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Main {
    /**
     * Usage: java -jar simulator.jar [<processes.json>] [cores] [quantum]
     * If no <processes.json> provided, tries to load default "processes.json".
     */
    public static void main(String[] args) throws Exception {
        String jsonPath;
        int cores = 2;
        int quantum = 4;

        if (args.length >= 1) {
            jsonPath = args[0];
            if (args.length >= 2) {
                cores = Integer.parseInt(args[1]);
            }
            if (args.length >= 3) {
                quantum = Integer.parseInt(args[2]);
            }
        } else {
            File f = new File("processes.json");
            if (f.exists()) {
                jsonPath = f.getName();
            } else {
                System.err.println("Arquivo 'processes.json' não encontrado no diretório atual.");
                System.err.println("Use: java -jar simulator.jar <processes.json> [cores] [quantum]");
                return;
            }
        }

        Gson gson = new Gson();
        Type listType = new TypeToken<List<CpuProcess>>() {}.getType();

        File file = new File(jsonPath);
        if (!file.exists()) {
            System.err.println("Arquivo \"" + jsonPath + "\" não encontrado "
                    + "nem no filesystem nem no classpath.");
            return;
        }

        Reader reader = new FileReader(file, StandardCharsets.UTF_8);

        List<CpuProcess> processes = gson.fromJson(reader, listType);

        // Se quantum for 0, usa dinâmico
        RoundRobinScheduler scheduler;
        if (quantum <= 0) {
            scheduler = new RoundRobinScheduler(processes, cores, new DynamicQuantumStrategy(4));
        } else {
            scheduler = new RoundRobinScheduler(processes, cores, new FixedQuantumStrategy(quantum));
        }

        scheduler.run();
        scheduler.printMetrics();
        scheduler.printTimeline();
        ChartGenerator.generateGantt(scheduler.timelineStrings(), "gantt.png");
    }
}