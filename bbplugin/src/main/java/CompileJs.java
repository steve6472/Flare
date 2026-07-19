import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by steve6472
 * Date: 7/19/2026
 * Project: Flare <br>
 *
 */

static File PLUGIN = new File("bbplugin");
static File JS_SOURCE = new File(PLUGIN, "jssource");
static File MAIN = new File(JS_SOURCE, "main.js");
static File OUTPUT = new File(PLUGIN, "flare_model.js");
static String INCLUDE = "//#include ";
static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

void main() throws IOException, InterruptedException
{
    compile();
    startWatching();
}

private void startWatching() throws IOException, InterruptedException
{
    WatchService watcher = FileSystems.getDefault().newWatchService();

    Path path = JS_SOURCE.toPath();

    path.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

    WatchKey key;
    while ((key = watcher.take()) != null)
    {
        key.pollEvents();
        key.reset();
        compile();
    }

    watcher.close();
}

private void compile() throws IOException
{
    String time = DATE_FORMAT.format(new Date());

    try
    {
        List<String> output = compile(MAIN, new ArrayList<>());
        if (!OUTPUT.createNewFile() && !OUTPUT.exists())
        {
            throw new IOException("Failed to create output file");
        }
        Files.write(OUTPUT.toPath(), output, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("[" + time + "] Compile successful");
    }
    catch (CompileException compileException)
    {
        System.err.print("[" + time + "] Failed compile: ");
        System.err.println(compileException.getMessage());
    }
}

private List<String> compile(File file, List<String> includedPaths) throws IOException, CompileException
{
    if (!file.exists())
    {
        throw new CompileException("File '" + file.getPath() + "' not found!");
    }

    List<String> input = Files.readAllLines(file.toPath());
    List<String> output = new ArrayList<>(input.size());

    for (String line : input)
    {
        if (!line.startsWith(INCLUDE))
        {
            output.add(line);
        } else
        {
            String filePath = line.substring(INCLUDE.length());
            if (!includedPaths.contains(filePath))
            {
                includedPaths.add(filePath);
                File includeFile = new File(JS_SOURCE, filePath);
                output.addAll(compile(includeFile, includedPaths));
            } else
            {
                System.err.println("Tried to include '" + filePath + "' multiple times");
            }
        }
    }

    return output;
}

static class CompileException extends RuntimeException
{
    public CompileException(String s)
    {
        super(s);
    }
}