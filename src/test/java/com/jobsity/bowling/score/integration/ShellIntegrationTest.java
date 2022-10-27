package com.jobsity.bowling.score.integration;

import com.jobsity.bowling.score.config.ClientTestRunner;
import com.jobsity.bowling.score.helper.ResourceHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.shell.Shell;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.shell.interactive.enabled=false")
@Import(ClientTestRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ShellIntegrationTest {

    private static final String SHELL_COMMAND = "score --source %s";
    public static final String POSITIVE_SCORES_FILE = "positive/scores.txt";
    public static final String PERFECT_SCORES_FILE = "positive/perfect.txt";
    public static final String ZERO_SCORES_FILE = "negative/empty.txt";
    public static final String EXTRA_SCORES_FILE = "negative/extra-score.txt";
    public static final String FREE_TEXT_SCORES_FILE = "negative/free-text.txt";
    public static final String INVALID_SCORES_FILE = "negative/invalid-score.txt";
    public static final String NEGATIVE_SCORES_FILE = "negative/negative.txt";


    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Autowired
    private Shell shell;

    @Autowired
    private ResourceHelper resourceHelper;

    @Test
    public void givenValidSourceWhenScoreThenShouldReturnProperReport() {
        // Arrange
        final String scoresFile = resourceHelper.getFileResourceAbsolutePath(POSITIVE_SCORES_FILE);

        // Act
        final String shellResult = (String)shell.evaluate(() -> buildShellCommand(scoresFile));

        // Assert
        errorCollector.checkThat(shellResult.contains("Jeff"), equalTo(true));
        errorCollector.checkThat(shellResult.contains("X\t7\t/\t9\t0\t\tX\t0\t8\t8\t/\tF\t6\t\tX\t\tX\t10\t8\t1"),
                equalTo(true));
        errorCollector.checkThat(shellResult.contains("20\t\t39\t\t48\t\t66\t\t74\t\t84\t\t90\t\t120\t\t148\t\t167"),
                equalTo(true));
        errorCollector.checkThat(shellResult.contains("John"), equalTo(true));
        errorCollector.checkThat(shellResult.contains("3\t/\t6\t3\t\tX\t8\t1\t\tX\t\tX\t9\t0\t7\t/\t4\t4\t10\t9\t0"),
                equalTo(true));
        errorCollector.checkThat(shellResult.contains("16\t\t25\t\t44\t\t53\t\t82\t\t101\t\t110\t\t124\t\t132\t\t151"),
                equalTo(true));
    }

    @Test
    public void givenValidSourceWithPerfectScoreWhenScoreThenShouldReturnProperReport() {
        // Arrange
        final String scoresFile = resourceHelper.getFileResourceAbsolutePath(PERFECT_SCORES_FILE);

        // Act
        final String shellResult = (String)shell.evaluate(() -> buildShellCommand(scoresFile));

        // Assert
        errorCollector.checkThat(shellResult.contains("Carl"), equalTo(true));
        assertPerfectResult(shellResult);
    }

    @Test
    public void givenEmptySourceWhenScoreThenShouldNotReport() {
        // Arrange
        final String scoresFile = resourceHelper.getFileResourceAbsolutePath(ZERO_SCORES_FILE);

        // Act
        final String shellResult = (String)shell.evaluate(() -> buildShellCommand(scoresFile));

        // Assert
        assertInvalidInput(shellResult);
    }

    @Test
    public void givenSourceExtraScoresWhenScoreThenShouldReportWithoutExtraData() {
        // Arrange
        final String scoresFile = resourceHelper.getFileResourceAbsolutePath(EXTRA_SCORES_FILE);

        // Act
        final String shellResult = (String)shell.evaluate(() -> buildShellCommand(scoresFile));

        // Assert
        assertPerfectResult(shellResult);
    }

    @Test
    public void givenFreeTextSourceWhenScoreThenShouldNotReport() {
        // Arrange
        final String scoresFile = resourceHelper.getFileResourceAbsolutePath(FREE_TEXT_SCORES_FILE);

        // Act
        final String shellResult = (String)shell.evaluate(() -> buildShellCommand(scoresFile));

        // Assert
        assertInvalidInput(shellResult);
    }

    @Test
    public void givenInvalidScoresSourceWhenScoreThenShouldNotReport() {
        // Arrange
        final String scoresFile = resourceHelper.getFileResourceAbsolutePath(INVALID_SCORES_FILE);

        // Act
        final String shellResult = (String)shell.evaluate(() -> buildShellCommand(scoresFile));

        // Assert
        assertInvalidInput(shellResult, "Value lorem is not a valid score");
    }

    @Test
    public void givenNegativeScoresSourceWhenScoreThenShouldNotReport() {
        // Arrange
        final String scoresFile = resourceHelper.getFileResourceAbsolutePath(NEGATIVE_SCORES_FILE);

        // Act
        final String shellResult = (String)shell.evaluate(() -> buildShellCommand(scoresFile));

        // Assert
        assertInvalidInput(shellResult, "Provided score -5 is not in the expected range");
    }

    private void assertInvalidInput(final String shellResult) {
        assertInvalidInput(shellResult, "The source does not contain valid scores");
    }

    private void assertInvalidInput(final String shellResult, final String message) {
        errorCollector.checkThat(shellResult, equalTo(String.format("Unable to complete the operation <%s>", message)));
    }

    private void assertPerfectResult(final String shellResult) {
        errorCollector.checkThat(shellResult.contains("X\t\tX\t\tX\t\tX\t\tX\t\tX\t\tX\t\tX\t\tX\t10\t10\t10"),
                equalTo(true));
        errorCollector.checkThat(shellResult.contains("30\t\t60\t\t90\t\t120\t\t150\t\t180\t\t210\t\t240\t\t270\t\t300"),
                equalTo(true));
    }

    private String buildShellCommand(final String source) {
        return String.format(SHELL_COMMAND, source);
    }
}
