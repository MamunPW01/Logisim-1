/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.plexers;

import com.cburch.logisim.LogisimVersion;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.tools.key.JoinedConfigurator;
import com.cburch.logisim.util.GraphicsUtil;
import java.awt.Color;
import java.awt.Graphics;

public class Multiplexer extends InstanceFactory {

    public Multiplexer() {
        super("Multiplexer", Strings.getter("multiplexerComponent"));
        setAttributes(
            new Attribute[]{
                StdAttr.FACING,
                Plexers.ATTR_SELECT_LOC,
                Plexers.ATTR_SELECT,
                StdAttr.WIDTH,
                Plexers.ATTR_DISABLED,
                Plexers.ATTR_ENABLE
            }, new Object[]{
                Direction.EAST,
                Plexers.SELECT_BOTTOM_LEFT,
                Plexers.DEFAULT_SELECT,
                BitWidth.ONE,
                Plexers.DISABLED_FLOATING,
                Boolean.TRUE
            }
        );
        setKeyConfigurator(JoinedConfigurator.create(
            new BitWidthConfigurator(Plexers.ATTR_SELECT, 1, 5, 0),
            new BitWidthConfigurator(StdAttr.WIDTH)
        ));
        setIconName("multiplexer.gif");
        setFacingAttribute(StdAttr.FACING);
    }

    static void drawSelectCircle(Graphics g, Bounds bounds, Location location) {
        int locationDelta = Math.max(bounds.getHeight(), bounds.getWidth()) <= 50 ? 8 : 6;
        Location circuitLoc;
        if (bounds.getHeight() >= bounds.getWidth()) { // vertically oriented
            if (location.getY() < bounds.getY() + bounds.getHeight() / 2) { // at top
                circuitLoc = location.translate(0, locationDelta);
            } else { // at bottom
                circuitLoc = location.translate(0, -locationDelta);
            }
        } else {
            if (location.getX() < bounds.getX() + bounds.getWidth() / 2) { // at left
                circuitLoc = location.translate(locationDelta, 0);
            } else { // at right
                circuitLoc = location.translate(-locationDelta, 0);
            }
        }
        g.setColor(Color.LIGHT_GRAY);
        g.fillOval(circuitLoc.getX() - 3, circuitLoc.getY() - 3, 6, 6);
    }

    @Override
    public Object getDefaultAttributeValue(Attribute<?> attribute, LogisimVersion version) {
        if (attribute == Plexers.ATTR_ENABLE) {
            int newer = version.compareTo(LogisimVersion.get(2, 6, 3, 220));
            return newer >= 0;
        } else {
            return super.getDefaultAttributeValue(attribute, version);
        }
    }

    @Override
    public Bounds getOffsetBounds(AttributeSet attributes) {
        Direction dir = attributes.getValue(StdAttr.FACING);
        BitWidth select = attributes.getValue(Plexers.ATTR_SELECT);
        int inputs = 1 << select.getWidth();
        if (inputs == 2) {
            return Bounds.create(-30, -20, 30, 40).rotate(Direction.EAST, dir, 0, 0);
        } else {
            int offs = -(inputs / 2) * 10 - 10;
            int length = inputs * 10 + 20;
            return Bounds.create(-40, offs, 40, length).rotate(Direction.EAST, dir, 0, 0);
        }
    }

    @Override
    public boolean contains(Location location, AttributeSet attributeSet) {
        Direction facing = attributeSet.getValue(StdAttr.FACING);
        return Plexers.contains(location, getOffsetBounds(attributeSet), facing);
    }

    @Override
    protected void configureNewInstance(Instance instance) {
        instance.addAttributeListener();
        updatePorts(instance);
    }

    @Override
    protected void instanceAttributeChanged(Instance instance, Attribute<?> attribute) {
        if (attribute == StdAttr.FACING || attribute == Plexers.ATTR_SELECT_LOC || attribute == Plexers.ATTR_SELECT) {
            instance.recomputeBounds();
            updatePorts(instance);
        } else if (attribute == StdAttr.WIDTH || attribute == Plexers.ATTR_ENABLE) {
            updatePorts(instance);
        } else if (attribute == Plexers.ATTR_DISABLED) {
            instance.fireInvalidated();
        }
    }

    private void updatePorts(Instance instance) {
        Direction dir = instance.getAttributeValue(StdAttr.FACING);
        Object selectLoc = instance.getAttributeValue(Plexers.ATTR_SELECT_LOC);
        BitWidth data = instance.getAttributeValue(StdAttr.WIDTH);
        BitWidth select = instance.getAttributeValue(Plexers.ATTR_SELECT);
        boolean enable = instance.getAttributeValue(Plexers.ATTR_ENABLE);

        int selMult = selectLoc == Plexers.SELECT_BOTTOM_LEFT ? 1 : -1;
        int inputs = 1 << select.getWidth();
        Port[] ports = new Port[inputs + (enable ? 3 : 2)];
        Location sel;
        if (inputs == 2) {
            Location end0;
            Location end1;
            if (dir == Direction.WEST) {
                end0 = Location.create(30, -10);
                end1 = Location.create(30, 10);
                sel = Location.create(20, selMult * 20);
            } else if (dir == Direction.NORTH) {
                end0 = Location.create(-10, 30);
                end1 = Location.create(10, 30);
                sel = Location.create(selMult * -20, 20);
            } else if (dir == Direction.SOUTH) {
                end0 = Location.create(-10, -30);
                end1 = Location.create(10, -30);
                sel = Location.create(selMult * -20, -20);
            } else {
                end0 = Location.create(-30, -10);
                end1 = Location.create(-30, 10);
                sel = Location.create(-20, selMult * 20);
            }
            ports[0] = new Port(end0.getX(), end0.getY(), Port.INPUT, data.getWidth());
            ports[1] = new Port(end1.getX(), end1.getY(), Port.INPUT, data.getWidth());
        } else {
            int dx = -(inputs / 2) * 10;
            int ddx = 10;
            int dy = -(inputs / 2) * 10;
            int ddy = 10;
            if (dir == Direction.WEST) {
                dx = 40;
                ddx = 0;
                sel = Location.create(20, selMult * (dy + 10 * inputs));
            } else if (dir == Direction.NORTH) {
                dy = 40;
                ddy = 0;
                sel = Location.create(selMult * dx, 20);
            } else if (dir == Direction.SOUTH) {
                dy = -40;
                ddy = 0;
                sel = Location.create(selMult * dx, -20);
            } else {
                dx = -40;
                ddx = 0;
                sel = Location.create(-20, selMult * (dy + 10 * inputs));
            }
            for (int i = 0; i < inputs; i++) {
                ports[i] = new Port(dx, dy, Port.INPUT, data.getWidth());
                dx += ddx;
                dy += ddy;
            }
        }
        Location en = sel.translate(dir, 10);
        ports[inputs] = new Port(sel.getX(), sel.getY(), Port.INPUT, select.getWidth());
        if (enable) {
            ports[inputs + 1] = new Port(en.getX(), en.getY(), Port.INPUT, BitWidth.ONE);
        }
        ports[ports.length - 1] = new Port(0, 0, Port.OUTPUT, data.getWidth());

        for (int i = 0; i < inputs; i++) {
            ports[i].setToolTip(Strings.getter("multiplexerInTip", "" + i));
        }
        ports[inputs].setToolTip(Strings.getter("multiplexerSelectTip"));
        if (enable) {
            ports[inputs + 1].setToolTip(Strings.getter("multiplexerEnableTip"));
        }
        ports[ports.length - 1].setToolTip(Strings.getter("multiplexerOutTip"));

        instance.setPorts(ports);
    }

    @Override
    public void propagate(InstanceState state) {
        BitWidth data = state.getAttributeValue(StdAttr.WIDTH);
        BitWidth select = state.getAttributeValue(Plexers.ATTR_SELECT);
        boolean enable = state.getAttributeValue(Plexers.ATTR_ENABLE);
        int inputs = 1 << select.getWidth();
        Value enabled = enable ? state.getPort(inputs + 1) : Value.TRUE;
        Value out;
        if (enabled == Value.FALSE) {
            Object opt = state.getAttributeValue(Plexers.ATTR_DISABLED);
            Value base = opt == Plexers.DISABLED_ZERO ? Value.FALSE : Value.UNKNOWN;
            out = Value.repeat(base, data.getWidth());
        } else if (enabled == Value.ERROR && state.isPortConnected(inputs + 1)) {
            out = Value.createError(data);
        } else {
            Value sel = state.getPort(inputs);
            if (sel.isFullyDefined()) {
                out = state.getPort(sel.toIntValue());
            } else if (sel.isErrorValue()) {
                out = Value.createError(data);
            } else {
                out = Value.createUnknown(data);
            }
        }
        state.setPort(inputs + (enable ? 2 : 1), out, Plexers.DELAY);
    }

    @Override
    public void paintGhost(InstancePainter painter) {
        Direction facing = painter.getAttributeValue(StdAttr.FACING);
        BitWidth select = painter.getAttributeValue(Plexers.ATTR_SELECT);
        Plexers.drawTrapezoid(painter.getGraphics(), painter.getBounds(), facing, select.getWidth() == 1 ? 10 : 20);
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        Graphics g = painter.getGraphics();
        Bounds bounds = painter.getBounds();
        Direction facing = painter.getAttributeValue(StdAttr.FACING);
        BitWidth select = painter.getAttributeValue(Plexers.ATTR_SELECT);
        boolean enable = painter.getAttributeValue(Plexers.ATTR_ENABLE);
        int inputs = 1 << select.getWidth();

        // draw stubs for select/enable inputs that aren't on instance boundary
        GraphicsUtil.switchToWidth(g, 3);
        boolean vertical = facing != Direction.NORTH && facing != Direction.SOUTH;
        Object selectLoc = painter.getAttributeValue(Plexers.ATTR_SELECT_LOC);
        int selMult = selectLoc == Plexers.SELECT_BOTTOM_LEFT ? 1 : -1;
        int dx = vertical ? 0 : -selMult;
        int dy = vertical ? selMult : 0;
        if (inputs == 2) { // draw select wire
            Location pt = painter.getInstance().getPortLocation(inputs);
            if (painter.getShowState()) {
                g.setColor(painter.getPort(inputs).getColor());
            }
            g.drawLine(pt.getX() - 2 * dx, pt.getY() - 2 * dy, pt.getX(), pt.getY());
        }
        if (enable) {
            Location portLocation = painter.getInstance().getPortLocation(inputs + 1);
            if (painter.getShowState()) {
                g.setColor(painter.getPort(inputs + 1).getColor());
            }
            int len = inputs == 2 ? 6 : 4;
            g.drawLine(portLocation.getX() - len * dx, portLocation.getY() - len * dy, portLocation.getX(),
                portLocation.getY());
        }
        GraphicsUtil.switchToWidth(g, 1);

        // draw a circle indicating where the select input is located
        Multiplexer.drawSelectCircle(g, bounds, painter.getInstance().getPortLocation(inputs));

        // draw a 0 indicating where the numbering starts for inputs
        int x0;
        int y0;
        int hAlign;
        if (facing == Direction.WEST) {
            x0 = bounds.getX() + bounds.getWidth() - 3;
            y0 = bounds.getY() + 15;
            hAlign = GraphicsUtil.H_RIGHT;
        } else if (facing == Direction.NORTH) {
            x0 = bounds.getX() + 10;
            y0 = bounds.getY() + bounds.getHeight() - 2;
            hAlign = GraphicsUtil.H_CENTER;
        } else if (facing == Direction.SOUTH) {
            x0 = bounds.getX() + 10;
            y0 = bounds.getY() + 12;
            hAlign = GraphicsUtil.H_CENTER;
        } else {
            x0 = bounds.getX() + 3;
            y0 = bounds.getY() + 15;
            hAlign = GraphicsUtil.H_LEFT;
        }
        g.setColor(Color.GRAY);
        GraphicsUtil.drawText(g, "0", x0, y0, hAlign, GraphicsUtil.V_BASELINE);

        // draw the trapezoid, "MUX" string, the individual ports
        g.setColor(Color.BLACK);
        Plexers.drawTrapezoid(g, bounds, facing, select.getWidth() == 1 ? 10 : 20);
        GraphicsUtil.drawCenteredText(g, "MUX",
            bounds.getX() + bounds.getWidth() / 2,
            bounds.getY() + bounds.getHeight() / 2);
        painter.drawPorts();
    }
}
