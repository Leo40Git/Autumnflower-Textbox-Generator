package adudecalledleo.aftbg.app.script;

import java.util.random.RandomGenerator;

import org.graalvm.polyglot.HostAccess;

public final class ScriptRandomAccess {
    static final ScriptRandomAccess INSTANCE = new ScriptRandomAccess();

    private final RandomGenerator delegate;

    private ScriptRandomAccess() {
        this.delegate = RandomGenerator.getDefault();
    }

    @HostAccess.Export
    public boolean nextBoolean() {
        return delegate.nextBoolean();
    }

    @HostAccess.Export
    public float nextFloat() {
        return delegate.nextFloat();
    }

    @HostAccess.Export
    public float nextFloat(float bound) {
        return delegate.nextFloat(bound);
    }

    @HostAccess.Export
    public float nextFloat(float origin, float bound) {
        return delegate.nextFloat(origin, bound);
    }

    @HostAccess.Export
    public double nextDouble() {
        return delegate.nextDouble();
    }

    @HostAccess.Export
    public double nextDouble(double bound) {
        return delegate.nextDouble(bound);
    }

    @HostAccess.Export
    public double nextDouble(double origin, double bound) {
        return delegate.nextDouble(origin, bound);
    }

    @HostAccess.Export
    public int nextInt() {
        return delegate.nextInt();
    }

    @HostAccess.Export
    public int nextInt(int bound) {
        return delegate.nextInt(bound);
    }

    @HostAccess.Export
    public int nextInt(int origin, int bound) {
        return delegate.nextInt(origin, bound);
    }

    @HostAccess.Export
    public long nextLong() {
        return delegate.nextLong();
    }

    @HostAccess.Export
    public long nextLong(long bound) {
        return delegate.nextLong(bound);
    }

    @HostAccess.Export
    public long nextLong(long origin, long bound) {
        return delegate.nextLong(origin, bound);
    }

    @HostAccess.Export
    public double nextGaussian() {
        return delegate.nextGaussian();
    }

    @HostAccess.Export
    public double nextGaussian(double mean, double stddev) {
        return delegate.nextGaussian(mean, stddev);
    }

    @HostAccess.Export
    public double nextExponential() {
        return delegate.nextExponential();
    }
}
