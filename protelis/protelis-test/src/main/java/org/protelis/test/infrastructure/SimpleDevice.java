package org.protelis.test.infrastructure;

import static org.junit.Assert.assertNotNull;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.math3.random.RandomGenerator;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.lang.datatype.Field;
import org.protelis.lang.datatype.Tuple;
import org.protelis.lang.datatype.impl.ArrayTupleImpl;
import org.protelis.vm.LocalizedDevice;
import org.protelis.vm.NetworkManager;
import org.protelis.vm.SpatiallyEmbeddedDevice;
import org.protelis.vm.TimeAwareDevice;
import org.protelis.vm.impl.AbstractExecutionContext;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * A simple implementation of a Protelis-based device, encapsulating a
 * ProtelisVM and a network interface.
 */
public class SimpleDevice extends AbstractExecutionContext
                implements SpatiallyEmbeddedDevice, LocalizedDevice, TimeAwareDevice {
    private final RandomGenerator r;
    private final ProtelisNode node;
    private final Environment<Object> env;
    private final Reaction<Object> react;
    private final NetworkManager netmgr;

    /**
     * 
     * @param environment
     *            environment
     * @param node
     *            node
     * @param reaction
     *            reaction
     * @param random
     *            random
     * @param netmgr
     *            netmgr
     */
    public SimpleDevice(final Environment<Object> environment, final ProtelisNode node, final Reaction<Object> reaction,
                    final RandomGenerator random, final NetworkManager netmgr) {
        super(node, netmgr);
        r = random;
        this.react = reaction;
        this.env = environment;
        this.netmgr = netmgr;
        this.node = node;
    }

    /**
     * Move in a direction specified by the 3-tuple vector in meters Uses a
     * kludge vector in which +X = East, +Y = North.
     * 
     * @param vector
     *            move the device of the given vector
     */
    public void move(final Tuple vector) {
        throw new NotImplementedException("move is not supported at the moment");
    }

    @Override
    public DeviceUID getDeviceUID() {
        assertNotNull("Device id can't be null", node);
        return node;
    }

    @Override
    public Number getCurrentTime() {
        return react.getTau().toDouble();
    }

    /*
     * ATTENTION: getDeltaTime has been overridden to easily test the results.
     * If you need to estimate the difference between two reactions you can
     * comment this method as it is already implemented in
     * org.protelis.vm.impl.AbstractExecutionContext. Doing so, tests related to
     * getDeltaTime will fail.
     * 
     * @see org.protelis.vm.impl.AbstractExecutionContext#getDeltaTime()
     */
    @Override
    public Number getDeltaTime() {
        return 1;
    }

    @Override
    protected AbstractExecutionContext instance() {
        return new SimpleDevice(env, node, react, r, netmgr);
    }

    /**
     * Note: this should be going away in the future, to be replaced by standard
     * Java random.
     * @return a double random value
     */
    @Override
    public double nextRandomDouble() {
        return r.nextDouble();
    }

    @Override
    public Field nbrRange() {
        return buildField(otherNode -> this.env.getDistanceBetweenNodes(node, (ProtelisNode) otherNode), node);
    }

    @Override
    public Field nbrLag() {
        return buildField(otherNode -> 1.0, node);
    }

    @Override
    public Tuple getCoordinates() {
        final double[] cd = env.getPosition(node).getCartesianCoordinates();
        Tuple c = new ArrayTupleImpl(0, cd.length);
        for (int i = 0; i < cd.length; i++) {
            c = c.set(i, cd[i]);
        }
        return c;
    }

    @Override
    public Field nbrVector() {
        return buildField(device -> this.getVectorToNeigh(getCoordinates(), device), this);
    }

    private Tuple getVectorToNeigh(final Tuple myC, final LocalizedDevice device) {
        final Tuple otherC = device.getCoordinates();
        Tuple tmp = device.getCoordinates();
        for (int i = 0; i < myC.size(); i++) {
            tmp = tmp.set(i, (Double) otherC.get(i) - (Double) myC.get(i));
        }
        return tmp;
    }
}
