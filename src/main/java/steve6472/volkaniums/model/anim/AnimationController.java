package steve6472.volkaniums.model.anim;

/**
 * Created by steve6472
 * Date: 9/10/2024
 * Project: Volkaniums <br>
 */
public class AnimationController
{
    private long start;
    private long pause;
    private boolean running, paused, loop, reversed, stayAtLastFrame;
    private double pauseTime;
    private double speed;

    public AnimationController()
    {
        this.speed = 1;
    }

    public void start()
    {
        start = System.currentTimeMillis();
        pause = 0;
        running = true;
    }

    public void toggleLoop()
    {
        this.loop = !loop;
    }

    public void setLoop(boolean loop)
    {
        this.loop = loop;
    }

    public void pause()
    {
        pauseTime = calculateTime(System.currentTimeMillis());
        pause = System.currentTimeMillis();
        paused = true;
    }

    public void setSpeed(double speed)
    {
        this.speed = speed;
    }

    public boolean isLooping()
    {
        return loop;
    }

    public boolean hasEnded()
    {
        return !running;
    }

    public void setRunning(boolean running)
    {
        this.running = running;
    }

    public boolean isPaused()
    {
        return paused;
    }

    public double getPauseTime()
    {
        return pauseTime;
    }

    public boolean isReversed()
    {
        return reversed;
    }

    public void setReverse(boolean reverse)
    {
        this.reversed = reverse;
    }

    public boolean isStayAtLastFrame()
    {
        return stayAtLastFrame;
    }

    public void setStayAtLastFrame(boolean stayAtLastFrame)
    {
        this.stayAtLastFrame = stayAtLastFrame;
    }

    public void unpuase()
    {
        start = start + (System.currentTimeMillis() - pause);
        paused = false;
    }

    public void togglePause()
    {
        if (paused)
            unpuase();
        else
            pause();
    }

    public double calculateTime(long end)
    {
        if (!running && !paused && !stayAtLastFrame)
            return 0;
        return (double) (end - start) / 1000d;
    }

    public double getSpeed()
    {
        return speed;
    }
}
