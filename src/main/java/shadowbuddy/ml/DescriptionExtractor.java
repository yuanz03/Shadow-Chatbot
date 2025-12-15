package shadowbuddy.ml;

import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * Extracts the description portion of a task using a trained classification model and its associated filter.
 * This utility class also provides an {@code extract} method which tokenises the input, classifies each word,
 * and returns only those labelled as {@code "description"} that appear after a {@code "command"} word.
 */
public class DescriptionExtractor {
    private static final String MODEL_FILE_PATH = "src/main/resources/models/description.model";
    private static final String FILTER_FILE_PATH = "src/main/resources/models/description-filter.model";

    private static final Object[] MODEL_AND_FILTER = loadModelAndFilter();
    private static final Classifier MODEL = (Classifier) MODEL_AND_FILTER[0];
    private static final StringToWordVector FILTER = (StringToWordVector) MODEL_AND_FILTER[1];
    private static final Instances STRUCTURE = createStructure();

    /**
     * Extracts the description portion from the provided task input.
     * Description extraction begins only after the first {@code "command"} word is encountered.
     *
     * @param input Raw task input from which the description is extracted.
     * @return A String representing the extracted task description.
     * @throws Exception If text preprocessing or intent classification fails.
     */
    public static String extract(String input) throws Exception {
        String[] inputDetails = input.split(" ");
        StringBuilder output = new StringBuilder();
        boolean isCommandFound = false; // flag to indicate that a "command" word has been encountered

        for (String detail : inputDetails) { // iterate over each word and classify it individually
            Instances data = new Instances(STRUCTURE, 0);
            data.setClassIndex(STRUCTURE.classIndex());

            DenseInstance instance = new DenseInstance(data.numAttributes());
            instance.setValue(data.attribute("text"), detail);
            instance.setDataset(data);
            data.add(instance);

            Instances filteredData = Filter.useFilter(data, FILTER);

            double result = MODEL.classifyInstance(filteredData.firstInstance());
            String label = filteredData.classAttribute().value((int) result);

            if (label.equals("command")) {
                isCommandFound = true; // set the boolean flag to true if the word is labelled as a "command"
                continue; // skip adding the "command" word to the output and move to the next word
            }

            if (isCommandFound && label.equals("description")) {
                output.append(detail).append(" ");
            } // append the word if a "command" word has already been seen and word is labelled as "description"
        }
        return output.toString().trim();
    }

    private static Object[] loadModelAndFilter() {
        try {
            Classifier model = (Classifier) SerializationHelper.read(MODEL_FILE_PATH);
            StringToWordVector filter = (StringToWordVector) SerializationHelper.read(FILTER_FILE_PATH);
            return new Object[] { model, filter };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Instances createStructure() {
        try {
            ArrayList<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute("text", (ArrayList<String>) null));

            ArrayList<String> classValues = new ArrayList<>();
            classValues.add("description");
            classValues.add("command");
            classValues.add("other");
            attributes.add(new Attribute("class", classValues));

            Instances structure = new Instances("DescriptionInstances", attributes, 0);
            structure.setClassIndex(1);
            return structure;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
