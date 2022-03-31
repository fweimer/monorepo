package enyo.build;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Callable;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.DocumentationTool;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

/**
 * Results of running the Java compiler and documentation tool.
 *
 * @param <S> type of source objects embedded in diagnostics
 * @param durationSeconds tool running time, measured in seconds
 * @param reportedSuccess the flag reported by the tool;
 *   see {@link #consolidatedSuccess()}
 * @param additionalOutput unstructed output from the tool, usually empty
 * @param diagnostics errors and warnings, empty on success
 * @param exception if not empty, contains a fatal exception that was
 *   thrown during the execution of the tool
 */
record ToolResults<S>(
    double durationSeconds,
    boolean reportedSuccess,
    String additionalOutput,
    List<Diagnostic<? extends S>> diagnostics,
    Optional<Throwable> exception) {

    ToolResults {
        diagnostics = List.copyOf(diagnostics);
    }

    /**
     * Function to obtain the (standard) file manager for a tool.
     *
     * @param <S> type of source objects embedded in diagnostics
     * @see JavaCompiler#getStandardFileManager(Diagnosticlistener,Locale,Charset)
     * @see DocumentationTool#getStandardFileManager(Diagnosticlistener,Locale,Charset)
     */
    @FunctionalInterface
    interface GetFileManager<S> {
        JavaFileManager getFileManager(
            DiagnosticListener<S> diagnosticListener,
            Locale locale, Charset charset);
    }

    /**
     * Function to obtain a task object for a tool.
     *
     * @param <S> type of source objects embedded in diagnostics
     * @param <T> type of the task object
     * @see JavaCompiler.CompilationTask
     * @see DocumentationTool.DocumentationTask
     */
    @FunctionalInterface
    interface GetTask<S, T extends Callable<Boolean>> {
        T getTask(Writer writer,
            DiagnosticListener<S> diagnosticListener,
            JavaFileManager fileMananger);
    }

    /**
     * Method to set the locale of a task.
     *
     * @param <T> the type of the task
     * @see JavaCompiler.CompilationTask#setLocale(Locale)
     * @see DocumentationTool.DocumentationTask#setLocale(Locale)
     */
    @FunctionalInterface
    interface SetLocale<T> {
        void setLocale(T task, Locale locale);
    }

    private static <S, T extends Callable<Boolean>> ToolResults<S> run(
        GetFileManager<S> fileManager, GetTask<S, T> task,
        SetLocale<T> setLocale) {
        long nanoStart = System.nanoTime();
        var locale = Locale.US;
        var writer = new StringWriter();
        var diagnosticListener = new DiagnosticCollector<S>();
        Throwable exception;
        boolean success;

        try {
            var taskObject = task.getTask(writer, diagnosticListener,
                fileManager.getFileManager(
                    diagnosticListener, locale, StandardCharsets.UTF_8));
            setLocale.setLocale(taskObject, locale);
            success = taskObject.call();
            exception = null;
        } catch (Throwable e) {
            exception = e;
            success = false;
        }
        long nanoEnd = System.nanoTime();

        return new ToolResults<S>(
            (nanoEnd - nanoStart) * 1e-9,
            success,
            writer.toString(),
            diagnosticListener.getDiagnostics(),
            Optional.ofNullable(exception));
    }

    static ToolResults<? super JavaFileObject> run(JavaCompiler compiler,
        List<String> options, List<String> classes,
        List<? extends JavaFileObject> compilationUnits) {
        return run(
            (diagnosticListener, locale, charset) ->
            compiler.getStandardFileManager(
                diagnosticListener, locale, charset),
            (writer, diagnosticListener, fileManager) -> {
                var task = compiler.getTask(
                    writer, fileManager, diagnosticListener,
                    options, classes, compilationUnits);
                return task;
            },
            (task, locale) -> task.setLocale(locale));
    }

    static ToolResults<? super JavaFileObject> run(DocumentationTool docTool,
        Class<?> docletClass, List<String> options,
        List<? extends JavaFileObject> compilationUnits) {
        return run(
            (diagnosticListener, locale, charset) ->
            docTool.getStandardFileManager(
                diagnosticListener, locale, charset),
            (writer, diagnosticListener, fileManager) -> {
                var task = docTool.getTask(
                    writer, fileManager, diagnosticListener,
                    docletClass, options, compilationUnits);
                return task;
            },
            (task, locale) -> task.setLocale(locale));
    }

    /**
     * Returns the consolidated result.
     *
     * A successful run does not have any errors or warnings, or
     * additional output.
     *
     * @return true if the overall tool status was successful
     */
    boolean consolidatedSuccess() {
        return reportedSuccess
            && additionalOutput.isEmpty()
            && diagnostics.isEmpty();
    }
}
