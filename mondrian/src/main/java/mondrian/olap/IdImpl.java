/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 1998-2005 Julian Hyde
// Copyright (C) 2005-2017 Hitachi Vantara and others
// All Rights Reserved.
*/

package mondrian.olap;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mondrian.olap.api.NameSegment;
import mondrian.olap.api.Quoting;
import mondrian.olap.api.Segment;

import org.eclipse.daanse.olap.api.query.component.Id;
import org.olap4j.impl.UnmodifiableArrayList;

import mondrian.mdx.MdxVisitor;
import mondrian.olap.type.Type;

/**
 * Multi-part identifier.
 *
 * @author jhyde, 21 January, 1999
 */
public class IdImpl
    extends ExpBase
    implements Cloneable, Id
{

    private final List<Segment> segments;

    /**
     * Creates an identifier containing a single part.
     *
     * @param segment Segment, consisting of a name and quoting style
     */
    public IdImpl(Segment segment) {
        segments = Collections.singletonList(segment);
    }

    public IdImpl(List<Segment> segments) {
        this.segments = segments;
        if (segments.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }

    @Override
	public IdImpl cloneExp() {
        // This is immutable, so no need to clone.
        return this;
    }

    @Override
	public int getCategory() {
        return Category.UNKNOWN;
    }

    @Override
	public Type getType() {
        // Can't give the type until we have resolved.
        throw new UnsupportedOperationException();
    }

    @Override
	public String toString() {
        final StringBuilder buf = new StringBuilder();
        Util.quoteMdxIdentifier(segments, buf);
        return buf.toString();
    }

    @Override
    public String[] toStringArray() {
        String[] names = new String[segments.size()];
        int k = 0;
        for (Segment segment : segments) {
            names[k++] = ((NameSegmentImpl) segment).getName();
        }
        return names;
    }

    @Override
    public List<Segment> getSegments() {
        return Collections.unmodifiableList(this.segments);
    }

    @Override
    public Segment getElement(int i) {
        return segments.get(i);
    }

    /**
     * Returns a new Identifier consisting of this one with another segment
     * appended. Does not modify this Identifier.
     *
     * @param segment Name of segment
     * @return New identifier
     */
    public IdImpl append(Segment segment) {
        List<Segment> newSegments = new ArrayList<>(segments);
        newSegments.add(segment);
        return new IdImpl(newSegments);
    }

    @Override
	public Exp accept(Validator validator) {
        if (segments.size() == 1) {
            final Segment s = segments.get(0);
            if (s.getQuoting() == Quoting.UNQUOTED) {
                NameSegmentImpl nameSegment = (NameSegmentImpl) s;
                if (validator.getFunTable().isReserved(nameSegment.getName())) {
                    return SymbolLiteralImpl.create(
                        nameSegment.getName().toUpperCase());
                }
            }
        }
        final Exp element =
            Util.lookup(
                validator.getQuery(),
                validator.getSchemaReader().withLocus(),
                segments,
                true);

        if (element == null) {
            return null;
        }
        return element.accept(validator);
    }

    @Override
	public Object accept(MdxVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
	public void unparse(PrintWriter pw) {
        pw.print(toString());
    }

    /**
     * Component in a compound identifier. It is described by its name and how
     * the name is quoted.
     *
     * <p>For example, the identifier
     * <code>[Store].USA.[New Mexico].&[45]</code> has four segments:<ul>
     * <li>"Store", {@link mondrian.olap.Quoting#QUOTED}</li>
     * <li>"USA", {@link mondrian.olap.Quoting#UNQUOTED}</li>
     * <li>"New Mexico", {@link mondrian.olap.Quoting#QUOTED}</li>
     * <li>"45", {@link mondrian.olap.Quoting#KEY}</li>
     * </ul>
     */
    public static abstract class AbstractSegment implements Segment {
        private final Quoting quoting;

        protected AbstractSegment(Quoting quoting) {
            this.quoting = quoting;
        }

        @Override
		public String toString() {
            final StringBuilder buf = new StringBuilder();
            toString(buf);
            return buf.toString();
        }

        @Override
        public Quoting getQuoting() {
            return quoting;
        }

        @Override
        public abstract List<NameSegment> getKeyParts();

        /**
         * Converts an array of names to a list of segments.
         *
         * @param nameParts Array of names
         * @return List of segments
         */
        public static List<AbstractSegment> toList(String... nameParts) {
            final List<AbstractSegment> segments =
                new ArrayList<>(nameParts.length);
            for (String namePart : nameParts) {
                segments.add(new NameSegmentImpl(namePart));
            }
            return segments;
        }

        /**
         * Returns whether this segment matches a given name according to
         * the rules of case-sensitivity and quoting.
         *
         * @param name Name to match
         * @return Whether matches
         */
        @Override
        public abstract boolean matches(String name);

        /**
         * Appends this segment to a StringBuilder.
         *
         * @param buf String builder to write to
         */
        @Override
        public abstract void toString(StringBuilder buf);
    }

    /**
     * Component in a compound identifier that describes the name of an object.
     * Optionally, the name is quoted in brackets.
     *
     * @see KeySegment
     */
    public static class NameSegmentImpl extends AbstractSegment implements NameSegment {
        private final String name;

        /**
         * Creates a name segment with the given quoting.
         *
         * @param name Name
         * @param quoting Quoting style
         */
        public NameSegmentImpl(String name, Quoting quoting) {
            super(quoting);
            this.name = name;
            if (name == null) {
                throw new NullPointerException();
            }
            if (!(quoting == Quoting.QUOTED || quoting == Quoting.UNQUOTED)) {
                throw new IllegalArgumentException();
            }
        }

        /**
         * Creates a quoted name segment.
         *
         * @param name Name
         */
        public NameSegmentImpl(String name) {
            this(name, Quoting.QUOTED);
        }

        @Override
		public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof NameSegmentImpl that)) {
                return false;
            }
            return that.name.equals(this.name);
        }

        @Override
		public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
		public List<NameSegment> getKeyParts() {
            return null;
        }

        @Override
		public void toString(StringBuilder buf) {
            switch (getQuoting()) {
            case UNQUOTED:
                buf.append(name);
                return;
            case QUOTED:
                Util.quoteMdxIdentifier(name, buf);
                return;
            default:
                throw Util.unexpected(getQuoting());
            }
        }

        @Override
		public boolean matches(String name) {
            switch (getQuoting()) {
            case UNQUOTED:
                return Util.equalName(this.name, name);
            case QUOTED:
                return Util.equalName(this.name, name);
            default:
                return false;
            }
        }
    }

    /**
     * Identifier segment representing a key, possibly composite.
     */
    public static class KeySegment extends AbstractSegment {
        public final List<NameSegment> subSegmentList;

        /**
         * Creates a KeySegment with one or more sub-segments.
         *
         * @param subSegments Array of sub-segments
         */
        public KeySegment(NameSegmentImpl... subSegments) {
            super(Quoting.KEY);
            if (subSegments.length < 1) {
                throw new IllegalArgumentException();
            }
            this.subSegmentList = UnmodifiableArrayList.asCopyOf(subSegments);
        }

        /**
         * Creates a KeySegment a list of sub-segments.
         *
         * @param subSegmentList List of sub-segments
         */
        public KeySegment(List<NameSegment> subSegmentList) {
            super(Quoting.KEY);
            if (subSegmentList.isEmpty()) {
                throw new IllegalArgumentException();
            }
            this.subSegmentList =
                new UnmodifiableArrayList<>(
                    subSegmentList.toArray(
                        new NameSegmentImpl[subSegmentList.size()]));
        }

        @Override
		public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof KeySegment that)) {
                return false;
            }
            return this.subSegmentList.equals(that.subSegmentList);
        }

        @Override
		public int hashCode() {
            return subSegmentList.hashCode();
        }

        @Override
		public void toString(StringBuilder buf) {
            for (NameSegment segment : subSegmentList) {
                buf.append('&');
                segment.toString(buf);
            }
        }

        @Override
		public List<NameSegment> getKeyParts() {
            return subSegmentList;
        }

        @Override
		public boolean matches(String name) {
            return false;
        }
    }

}