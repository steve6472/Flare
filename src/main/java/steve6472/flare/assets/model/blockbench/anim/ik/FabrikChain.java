package steve6472.flare.assets.model.blockbench.anim.ik;

import org.joml.Vector3f;

import java.util.Arrays;

// Ensure chain has at least one segment ?
public record FabrikChain(FabrikSegment... segments)
{
    public static final int MAX_ITERATIONS = 200;
    public static final float MIN_ERROR = 1e-4f;

    public static FabrikChain fromVerticies(Vector3f... points)
    {
        FabrikSegment[] segments = new FabrikSegment[points.length - 1];

        for (int i = 0; i < points.length - 1; i++)
        {
            segments[i] = new FabrikSegment(points[i], points[i + 1]);
        }

        return new FabrikChain(segments);
    }

    public void solve(Vector3f endEffector)
    {
        Vector3f start = new Vector3f(segments[0].pos());

//        if (!canBeSolved(endEffector))
//        {
//            stretchTowards(endEffector);
//            return;
//        }

        for (int i = 0; i < MAX_ITERATIONS; i++)
        {
            backwards(endEffector);
            forwards(start);

            if (calculateError(endEffector) <= MIN_ERROR)
            {
                return;
            }
        }
    }

    private float calculateError(Vector3f endEffector)
    {
        return fromTheBack(0).other().distance(endEffector);
    }

    private void forwards(Vector3f target)
    {
        Vector3f lastSet = target;

        for (int i = 0; i < segments.length; i++)
        {
            segments[i].pos().set(lastSet);

            Vector3f pos = new Vector3f(segments[i].other());
            pos.sub(segments[i].pos());
            pos.normalize();
            pos.mul(segments[i].targetLen());
            pos.add(lastSet);
            segments[i].other().set(pos);
            lastSet = pos;
        }
    }

    private void backwards(Vector3f endEffector)
    {
        Vector3f lastSet = endEffector;

        for (int i = 0; i < segments.length; i++)
        {
            fromTheBack(i).other().set(lastSet);

            Vector3f pos = new Vector3f(fromTheBack(i).pos());
            pos.sub(fromTheBack(i).other());
            pos.normalize();
            pos.mul(fromTheBack(i).targetLen());
            pos.add(lastSet);
            fromTheBack(i).pos().set(pos);
            lastSet = pos;
        }
    }

    private FabrikSegment fromTheBack(int index)
    {
        return segments[segments.length - index - 1];
    }

    // TODO: fix this method
    private void stretchTowards(Vector3f endEffector)
    {
        Vector3f dir = new Vector3f(segments[0].pos());
        endEffector.sub(dir, dir);
        dir.normalize();
        
        Vector3f lastPos = new Vector3f(segments[0].pos());
        float totalLen = 0;
        for (FabrikSegment segment : segments)
        {
            segment.pos().set(lastPos);
            Vector3f add = new Vector3f(dir);
            add.mul(totalLen + segment.targetLen());
            segment.other().set(add);
            totalLen += segment.targetLen();
            lastPos = new Vector3f(segment.other());
        }
    }

    public boolean canBeSolved(Vector3f endEffector)
    {
        return segments[0].pos().distance(endEffector) <= calculateChainLen();
    }

    public float calculateChainLen()
    {
        float len = 0;
        for (FabrikSegment segment : segments)
            len += segment.targetLen();
        return len;
    }

    public FabrikChain copy()
    {
        FabrikSegment[] segmentsCopy = new FabrikSegment[segments.length];
        for (int i = 0; i < segments.length; i++)
        {
            segmentsCopy[i] = segments[i].copy();
        }
        return new FabrikChain(segmentsCopy);
    }

    @Override
    public String toString()
    {
        return Arrays.toString(segments);
    }
}