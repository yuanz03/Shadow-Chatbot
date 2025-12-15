package shadowbuddy.ml;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * Trains a description classification model using Weka, a Java-based machine learning library.
 * This utility class loads training data from an ARFF file, Weka's native dataset format.
 * Raw user text input is then preprocessed using the {@code StringToWordVector} filter, which
 * converts string-based input into numeric attributes suitable for machine learning algorithms.
 * A {@code NaiveBayes} classification algorithm is then trained on the filtered data to predict
 * the intent associated with each user input. Finally, the trained classifier and the text filter
 * are saved to disk for future use.
 */
public class DescriptionTrainer {
    private static final String TRAINING_FILE_PATH = "src/main/resources/dataset/description.arff";
    private static final String MODEL_FILE_PATH = "src/main/resources/models/description.model";
    private static final String FILTER_FILE_PATH = "src/main/resources/models/description-filter.model";

    /**
     * Provides the main entry point of the DescriptionTrainer program.
     *
     * @throws Exception If reading data, building the classifier and filter, or persisting files fails.
     */
    public static void main(String[] args) throws Exception {
        Instances data = DataSource.read(TRAINING_FILE_PATH);
        data.setClassIndex(data.numAttributes() - 1);

        StringToWordVector filter = new StringToWordVector();
        filter.setLowerCaseTokens(true);
        filter.setOutputWordCounts(true);
        filter.setWordsToKeep(1000);

        filter.setInputFormat(data);
        Instances filteredData = Filter.useFilter(data, filter);
        filteredData.setClassIndex(filteredData.attribute("class").index());

        Classifier classifier = new NaiveBayes();
        classifier.buildClassifier(filteredData);

        SerializationHelper.write(MODEL_FILE_PATH, classifier);
        SerializationHelper.write(FILTER_FILE_PATH, filter);
    }
}
