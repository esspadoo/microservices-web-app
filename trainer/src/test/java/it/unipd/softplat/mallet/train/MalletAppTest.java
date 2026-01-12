package it.unipd.softplat.mallet.train;

import cc.mallet.pipe.Pipe;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * <strong> Class MalletAppTest </strong>
 *
 * <p><b>Summary</b>:
 * This test suite validates the core functionalities of the {@link MalletApp} class.
 * It focuses on testing the static methods {@code createInstanceList} and {@code trainTopicModel}
 * in isolation, ensuring they correctly interact with their dependencies and produce expected outputs.
 * <p><b>Test Suite Design</b>:
 * This is a unit test suite that tests the {@link MalletApp} by mocking its external dependencies
 * such as {@link MalletPipeFactory}, {@link DocumentIterator}, {@link InstanceList}, and {@link ParallelTopicModel}.
 * Mockito is used for mocking static methods and objects to control their behavior and verify interactions.
 */
@ExtendWith(MockitoExtension.class)
public class MalletAppTest {

    @Mock
    private Pipe mockPipe;

    private MockedStatic<MalletPipeFactory> mockedMalletPipeFactory;
    private MockedConstruction<InstanceList> mockedConstructionInstanceList;
    private MockedConstruction<ParallelTopicModel> mockedConstructionParallelTopicModel;
    private MockedConstruction<DocumentIterator> mockedConstructionDocumentIterator;


    @BeforeEach
    void setUp() {
        mockedMalletPipeFactory = Mockito.mockStatic(MalletPipeFactory.class);
        mockedMalletPipeFactory.when(() -> MalletPipeFactory.build(any(File.class))).thenReturn(mockPipe);
        mockedConstructionDocumentIterator = Mockito.mockConstruction(DocumentIterator.class);
        mockedConstructionInstanceList = Mockito.mockConstruction(InstanceList.class, (mock, context) -> {
            when(mock.size()).thenReturn(50);
        });
        mockedConstructionParallelTopicModel = Mockito.mockConstruction(ParallelTopicModel.class);
    }

    @AfterEach
    void tearDown() {
        mockedMalletPipeFactory.close();
        mockedConstructionDocumentIterator.close();
        mockedConstructionInstanceList.close();
        mockedConstructionParallelTopicModel.close();
    }

    /**
     * <p><b>Summary:</b> Tests the {@code createInstanceList} method for correct instance list creation.
     * <p><b>Test Case Design:</b> A dummy input stream and file are provided. The test verifies that
     * {@code MalletPipeFactory.build} is called, a new {@code InstanceList} is created with the built pipe,
     * and {@code addThruPipe} is invoked with a {@code DocumentIterator}.
     * <p><b>Test Description:</b> This test ensures that the {@code createInstanceList} method correctly
     * orchestrates the creation of a Mallet {@code InstanceList} by utilizing the {@code MalletPipeFactory}
     * and adding documents via a {@code DocumentIterator}.
     * <p><b>Pre-Condition:</b> Static methods for {@code MalletPipeFactory} are mocked. Constructors for
     * {@code InstanceList} and {@code DocumentIterator} are mocked using {@code MockedConstruction}.
     * <p><b>Post-Condition:</b> The returned {@code InstanceList} is not null, and the mocked methods
     * are verified to have been called with the expected arguments.
     * <p><b>Expected Results:</b> An {@code InstanceList} object is returned, and the interactions
     * with mocked dependencies are as expected.
     */
    @Test
    void testCreateInstanceList() {
        InputStream dataInputStream = new ByteArrayInputStream("test data".getBytes(StandardCharsets.UTF_8));
        File stoplist = new File("stoplist.txt");

        InstanceList result = MalletApp.createInstanceList(dataInputStream, stoplist);

        assertNotNull(result);
        mockedMalletPipeFactory.verify(() -> MalletPipeFactory.build(stoplist), times(1));

        // Verify that InstanceList constructor was called
        assertEquals(1, mockedConstructionInstanceList.constructed().size());
        InstanceList constructedInstanceList = mockedConstructionInstanceList.constructed().get(0);
        assertEquals(constructedInstanceList, result);

        // Verify that DocumentIterator constructor was called
        assertEquals(1, mockedConstructionDocumentIterator.constructed().size());
        DocumentIterator constructedDocumentIterator = mockedConstructionDocumentIterator.constructed().get(0);

        // Verify addThruPipe on the constructed InstanceList mock with the constructed DocumentIterator mock
        verify(constructedInstanceList, times(1)).addThruPipe(constructedDocumentIterator);
    }

    /**
     * <p><b>Summary:</b> Tests the {@code trainTopicModel} method for correct model training.
     * <p><b>Test Case Design:</b> A mocked {@code InstanceList} is provided along with training parameters.
     * The test verifies that a {@code ParallelTopicModel} is instantiated, instances are added,
     * and training parameters (numThreads, numIterations, topicDisplay) are set before {@code estimate()} is called.
     * <p><b>Test Description:</b> This test ensures that the {@code trainTopicModel} method correctly
     * configures and trains a {@code ParallelTopicModel} using the provided {@code InstanceList} and parameters.
     * <p><b>Pre-Condition:</b> The constructor for {@code ParallelTopicModel} is mocked using {@code MockedConstruction}.
     * An {@code InstanceList} mock is created directly for input.
     * <p><b>Post-Condition:</b> A trained {@code ParallelTopicModel} is returned, and the interactions
     * with mocked dependencies are as expected.
     * <p><b>Expected Results:</b> A {@code ParallelTopicModel} object is returned, and the training
     * methods are called in the correct sequence.
     * @throws IOException if an I/O error occurs during the test (unexpected in a unit test)
     */
    @Test
    void testTrainTopicModel() throws IOException {
        int numTopics = 10;
        int numIterations = 100;
        int numTopWords = 10;

        // Create a mock InstanceList to pass as an argument to trainTopicModel
        InstanceList inputInstanceList = mock(InstanceList.class);
        when(inputInstanceList.size()).thenReturn(50); // Simulate some instances for thread calculation

        ParallelTopicModel result = MalletApp.trainTopicModel(inputInstanceList, numTopics, numIterations, numTopWords);

        assertNotNull(result);

        // Verify that ParallelTopicModel constructor was called
        assertEquals(1, mockedConstructionParallelTopicModel.constructed().size());
        ParallelTopicModel constructedTopicModel = mockedConstructionParallelTopicModel.constructed().get(0);
        assertEquals(constructedTopicModel, result);

        verify(constructedTopicModel, times(1)).addInstances(inputInstanceList);
        verify(constructedTopicModel, times(1)).setNumThreads(anyInt()); // numThreads is calculated dynamically
        verify(constructedTopicModel, times(1)).setNumIterations(numIterations);
        verify(constructedTopicModel, times(1)).setTopicDisplay(100, numTopWords);
        verify(constructedTopicModel, times(1)).estimate();
    }
}
