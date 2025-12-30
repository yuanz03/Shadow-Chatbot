package shadowbuddy.ml;

import java.io.InputStream;
import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * Loads a trained intent classification model and its associated text preprocessing filter.
 * This utility class creates an {@code Instances} structure that mirrors the training dataset format,
 * as well as a {@code classify} method to convert raw user text input into a Weka Instance,
 * apply the configured preprocessing filter, and obtain the predicted class label.
 */
public class IntentClassifier {
    private static final String MODEL_FILE_PATH = "models/shadow.model";
    private static final String FILTER_FILE_PATH = "models/filter.model";

    /* Static initialiser ensures the model and filter are available once the class is first referenced */
    private static final Object[] MODEL_AND_FILTER = loadModelAndFilter();
    private static final Classifier MODEL = (Classifier) MODEL_AND_FILTER[0];
    private static final StringToWordVector FILTER = (StringToWordVector) MODEL_AND_FILTER[1];
    private static final Instances STRUCTURE = createStructure();

    /**
     * Classifies a single input string and returns the predicted intent label.
     *
     * @param input Raw user text to classify into the appropriate intent label.
     * @return A String representing the predicted intent label.
     * @throws Exception If text preprocessing or intent classification fails.
     */
    public static String classify(String input) throws Exception {
        /* Initialises a new empty Instances object with the same structure as the training dataset. */
        Instances data = new Instances(STRUCTURE, 0); // initial capacity set at 0
        data.setClassIndex(STRUCTURE.classIndex());

        /* Initialises a new DenseInstance object sized to the number of attributes in the Instances. */
        DenseInstance instance = new DenseInstance(data.numAttributes());

        /* Populates the text attribute value, at index 0, to the provided user input string. */
        instance.setValue(data.attribute(0), input);

        /* Links the instance with the dataset structure so that the filter and classifier can access it. */
        instance.setDataset(data);
        data.add(instance); // adds the populated instance into the Instances container

        Instances filteredData = Filter.useFilter(data, FILTER);

        /* Invokes classifier to predict the class label index for the first filtered instance. */
        double result = MODEL.classifyInstance(filteredData.firstInstance());

        /* Maps the numeric class index back to its corresponding class value and returns this label string. */
        return filteredData.classAttribute().value((int) result);
    }

    /**
     * Loads the previously saved classifier and filter models from disk.
     *
     * @return An Object array where index 0 is the {@code Classifier} and index 1 is the {@code StringToWordVector}.
     */
    private static Object[] loadModelAndFilter() {
        try {
            InputStream modelStream = DescriptionExtractor.class.getClassLoader().getResourceAsStream(MODEL_FILE_PATH);
            InputStream filterStream =
                    DescriptionExtractor.class.getClassLoader().getResourceAsStream(FILTER_FILE_PATH);

            Classifier model = (Classifier) SerializationHelper.read(modelStream);
            StringToWordVector filter = (StringToWordVector) SerializationHelper.read(filterStream);
            return new Object[] { model, filter };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructs an empty {@code Instances} object that exactly matches the structure used by the dataset.
     * All attributes used to create the training ARFF file must also match.
     *
     * @return A new {@code Instances} object with attributes "text" and "class" configured.
     */
    private static Instances createStructure() {
        try {
            ArrayList<Attribute> attributes = new ArrayList<>();

            /* Adds string attribute named "text" to attributes list, where null indicates it is a string attribute. */
            attributes.add(new Attribute("text", (ArrayList<String>) null));

            /* Constructs the nominal class attribute with all possible intent labels in the training dataset. */
            ArrayList<String> classValues = new ArrayList<>();
            classValues.add("query");
            classValues.add("mark");
            classValues.add("unmark");
            classValues.add("delete");
            classValues.add("todo");
            classValues.add("deadline");
            classValues.add("event");
            classValues.add("unknown");

            /* Adds class attribute named "class" to attributes list, with its predefined possible class values. */
            attributes.add(new Attribute("class", classValues));

            /* Initialises a new Instances object with the attributes list, set at an initial capacity of 0. */
            Instances structure = new Instances("IntentInstances", attributes, 0);
            structure.setClassIndex(1); // sets the class index to the class attribute's position of 1
            return structure;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
