package ch.ethz.matsim.external_costs.writers;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

public class EmissionsPerPersonWriter {
    private Map<Id<Person>, Map<String, Double>> perPersonItems;
    private String[] pollutants = new String[]{
            "CO2(total)", "CH4", "N2O",
            "PM", "NOx", "SO2",
            "NO2", "HC", "CO", "NMHC", "FC"
    };

    public EmissionsPerPersonWriter(Map<Id<Person>, Map<String, Double>> perPersonItems) {
        this.perPersonItems = perPersonItems;
    }

    public void write(String outputPath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));

        writer.write(formatHeader() + "\n");
        writer.flush();

        for (Id<Person> personId : this.perPersonItems.keySet()) {
            writer.write(formatItem(personId, this.perPersonItems.get(personId)) + "\n");
            writer.flush();
        }

        writer.flush();
        writer.close();

    }

    private String formatHeader() {
        return String.join(";", new String[]{"personId", String.join(";", this.pollutants)});
    }

    private String formatItem(Id<Person> personId, Map<String, Double> emissions) {
        String[] entry = new String[this.pollutants.length + 1];
        entry[0] = personId.toString();
        int i = 1;
        for (String pollutant : this.pollutants) {
            entry[i] = Double.toString(emissions.getOrDefault(pollutant, 0.0));
            i++;
        }
        return String.join(";", entry);
    }
}
