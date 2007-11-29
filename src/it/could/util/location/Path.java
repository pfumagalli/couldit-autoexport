/* ========================================================================== *
 *   Copyright (c) 2006, Pier Paolo Fumagalli <mailto:pier@betaversion.org>   *
 *                            All rights reserved.                            *
 * ========================================================================== *
 *                                                                            * 
 * Redistribution and use in source and binary forms, with or without modifi- *
 * cation, are permitted provided that the following conditions are met:      *
 *                                                                            * 
 *  - Redistributions of source code must retain the  above copyright notice, *
 *    this list of conditions and the following disclaimer.                   *
 *                                                                            * 
 *  - Redistributions  in binary  form  must  reproduce the  above  copyright *
 *    notice,  this list of conditions  and the following  disclaimer  in the *
 *    documentation and/or other materials provided with the distribution.    *
 *                                                                            * 
 *  - Neither the name of Pier Fumagalli, nor the names of other contributors *
 *    may be used to endorse  or promote products derived  from this software *
 *    without specific prior written permission.                              *
 *                                                                            * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" *
 * AND ANY EXPRESS OR IMPLIED WARRANTIES,  INCLUDING, BUT NOT LIMITED TO, THE *
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE *
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER  OR CONTRIBUTORS BE *
 * LIABLE  FOR ANY  DIRECT,  INDIRECT,  INCIDENTAL,  SPECIAL,  EXEMPLARY,  OR *
 * CONSEQUENTIAL  DAMAGES  (INCLUDING,  BUT  NOT LIMITED  TO,  PROCUREMENT OF *
 * SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR PROFITS;  OR BUSINESS *
 * INTERRUPTION)  HOWEVER CAUSED AND ON  ANY THEORY OF LIABILITY,  WHETHER IN *
 * CONTRACT,  STRICT LIABILITY,  OR TORT  (INCLUDING NEGLIGENCE OR OTHERWISE) *
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE *
 * POSSIBILITY OF SUCH DAMAGE.                                                *
 * ========================================================================== */
package it.could.util.location;

import it.could.util.StringTools;
import it.could.util.encoding.Encodable;
import it.could.util.encoding.EncodingTools;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;


/**
 * <p>The {@link Path} class is an immutable {@link List} of {@link PathElement}
 * instances representing a path structure.</p>
 *
 * @author <a href="http://could.it/">Pier Fumagalli</a>
 */
public class Path extends AbstractList implements Encodable {

    /** <p>The array of {@link PathElement}s.</p> */ 
    private final PathElement paths[];
    /** <p>A flag indicating whether this path is absolute or not.</p> */
    private final boolean absolute;
    /** <p>A flag indicating whether this path represents a collection.</p> */
    private final boolean collection;
    /** <p>The {@link String} representation of this (encoded).</p> */
    private final String string;

    /**
     * <p>Create a new {@link Path} instance.</p>
     * 
     * @param absolute wether the path represented by this instance is absolute
     *                 (starts at the "root" of the path structure) or not.
     * @throws NullPointerException if the {@link List} was <b>null</b>.
     * @throws ClassCastException if any of the elements in the {@link List}
     *                            was not a {@link PathElement}.
     */
    public Path(List elements, boolean absolute) {
        if (elements == null) throw new NullPointerException("Null list");

        /* Simplify the current path */
        final Stack resolved = resolve(null, absolute, elements);
        final PathElement array[] = new PathElement[resolved.size()];
        this.paths = (PathElement []) resolved.toArray(array);
        this.absolute = absolute;

        /* Cache wether we are a collection or not */
        this.collection = this.paths.length == 0 ? false :
                          this.paths[this.paths.length-1].getName().equals(".");
        
        /* Figure out our string representation (this uses collection above) */
        this.string = EncodingTools.toString(this);
    }

    /* ====================================================================== */
    /* STATIC CONSTRUCTION METHODS                                            */
    /* ====================================================================== */

    /**
     * <p>Parse a {@link String} into a {@link Path} structure.</p>
     *
     * @see #parse(String, String)
     * @return a <b>non-null</b> {@link Path} instance.
     */
    public static Path parse(String path) {
        try {
            return parse(path, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException exception) {
            final String message = "Unsupported encoding " + DEFAULT_ENCODING;
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /**
     * <p>Parse the specified {@link String} into a {@link Path} structure.</p>
     *
     * @return a <b>non-null</b> {@link Path} instance.
     */
    public static Path parse(String path, String encoding)
    throws UnsupportedEncodingException {
        final List elems = new ArrayList();

        /* No path? We consider this to be the empty path (with no elements) */
        if ((path == null) || (path.length() == 0))
            return new Path(elems, false);

        /* Check for a proper encoding */
        if (encoding == null) encoding = DEFAULT_ENCODING;

        /* Split up the path structure into its path element components */
        final String split[] = StringTools.splitAll(path, '/');

        /* Check every single path element and append it to the current one */
        PathElement element = null;
        for (int x = 0; x < split.length; x++) {
            if (split[x] == null) continue; /* Collapse double slashes */
            element = PathElement.parse(split[x], encoding); 
            if (element != null) elems.add(element);
        }

        /* If the split ended with a null, it ended with a slash character */
        if (split[split.length - 1] == null) elems.add(new PathElement(".")); 

        /* Setup the path list, and if the first split was null its absolute */
        return new Path(elems, split[0] == null);
    }

    /* ====================================================================== */
    /* RESOLUTION METHODS                                                     */
    /* ====================================================================== */

    /**
     * <p>Parse the specified {@link String} into a {@link Path} and resolve it
     * against this one.</p>
     * 
     * @see #resolve(String, String)
     * @see #resolve(Path)
     * @return a <b>non-null</b> {@link Path} instance.
     */
    public Path resolve(String path) {
        try {
            return this.resolve(parse(path, DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException exception) {
            final String message = "Unsupported encoding " + DEFAULT_ENCODING;
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /**
     * <p>Parse the specified {@link String} into a {@link Path} using the
     * specified character encoding and resolve it against this one.</p>
     * 
     * @see #resolve(Path)
     * @return a <b>non-null</b> {@link Path} instance.
     */
    public Path resolve(String path, String encoding)
    throws UnsupportedEncodingException {
        if (encoding == null) encoding = DEFAULT_ENCODING;
        if (path == null) return this;
        return this.resolve(parse(path, encoding));
    }

    /**
     * <p>Resolve the specified {@link Path} against this one.</p>
     * 
     * @return a <b>non-null</b> {@link Path} instance.
     */
    public Path resolve(Path path) {

        /* No path, return this instance */
        if (path == null) return this;

        /* If the target is absolute, only merge the parameters */ 
        if (path.absolute) return path;

        /* Resolve the path */
        final Stack source = new Stack();
        source.addAll(this);
        final List resolved = resolve(source, this.absolute, path);

        /* Figure out if the resolved path is a collection and return it */
        return new Path(resolved, this.absolute);
    }

    /* ====================================================================== */
    
    /**
     * <p>Simplify the specified list of {@link PathElement}s against the
     * {@link Stack} of {@link PathElement}s which might be absolute.
     */
    private static Stack resolve(Stack stack, boolean absolute, List elements) {

        /* If we have no source stack we create a new empty one */
        if (stack == null) stack = new Stack();
        if (elements.size() == 0) return stack;

        /* We always start from the collection of the last in the stack */
        if (! stack.empty()) stack.pop();

        /* A flag indicating whether we are at the "root" path element. */
        boolean atroot = absolute && stack.empty();
        
        /* A way to remember the last processed path element */
        PathElement element = null; 

        /* Iterate through the current path elements to see what to do. */
        for (Iterator iter = elements.iterator(); iter.hasNext(); ) {
            element = (PathElement) iter.next();

            /* If this is the "." (current) path element, skip it. */
            if (".".equals(element.getName())) continue;

            /* If this is the ".." (parent) path element, it gets nasty. */
            if ("..".equals(element.getName())) {
                
                /* The root path's parent is always itself */
                if (atroot) continue;
                
                /* We're not at root and have the stack, relative ".." */
                if (stack.size() == 0) {
                    stack.push(element);
                
                /* We're not at root, but we have stuff in the stack */
                } else {

                    /* Get the last element in the stack */
                    final PathElement prev = (PathElement) stack.peek();
                    /* If the last element is "..", add another one */
                    if ("..".equals(prev.getName())) stack.push(element);
                    /* The last element was not "..", pop it out */
                    else stack.pop();
                    /* If absoulte and stack is empty, we're at root */
                    if (absolute) atroot = stack.size() == 0;
                }

            /* Normal element processing follows (no "." or "..") */
            } else {
                stack.push(element);
                atroot = false;
            }
        }

        /* Check if the last element ment somehow a collection */
        if (element != null) {
            final String name = element.getName();
            if (name.equals("..")) stack.push(new PathElement("."));
            else if (name.equals(".")) stack.push(element);
        }

        /* Now return the stack */
        return stack;
    }

    /* ====================================================================== */
    /* RELATIVIZATION METHODS                                                 */
    /* ====================================================================== */
    
    /**
     * <p>Parse the specified {@link String} into a {@link Path} and relativize
     * it against this one.</p>
     * 
     * <p>This method is equivalent to a call to
     * {@link #relativize(String, boolean) relativize(path, true)}.</p>
     * 
     * @see #relativize(Path, boolean)
     * @return a <b>non-null</b> {@link Path} instance.
     */
    public Path relativize(String path) {
        try {
            return this.relativize(parse(path, DEFAULT_ENCODING), true);
        } catch (UnsupportedEncodingException exception) {
            final String message = "Unsupported encoding " + DEFAULT_ENCODING;
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /**
     * <p>Parse the specified {@link String} into a {@link Path} and relativize
     * it against this one, indicating whether {@link Path}s starting with
     * <code>&#2e;&#2e;</code> (parent) are allowed results or not.</p>
     * 
     * @see #relativize(Path, boolean)
     * @param allowParent if {@link Path}s starting with <code>&#2e;&#2e;</code>
     *                    (parent) are allowed results or not.
     * @return a <b>non-null</b> {@link Path} instance.
     */
    public Path relativize(String path, boolean allowParent) {
        try {
            return this.relativize(parse(path, DEFAULT_ENCODING), allowParent);
        } catch (UnsupportedEncodingException exception) {
            final String message = "Unsupported encoding " + DEFAULT_ENCODING;
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /**
     * <p>Parse the specified {@link String} into a {@link Path} using the
     * specified character encoding and relativize it against this one.</p>
     * 
     * <p>This method is equivalent to a call to
     * {@link #relativize(String, String, boolean)
     * relativize(path,encoding,true)}.</p>
     * 
     * @see #relativize(Path, boolean)
     * @return a <b>non-null</b> {@link Path} instance.
     */
    public Path relativize(String path, String encoding)
    throws UnsupportedEncodingException {
        if (encoding == null) encoding = DEFAULT_ENCODING;
        return this.relativize(parse(path, encoding));
    }

    /**
     * <p>Parse the specified {@link String} into a {@link Path} using the
     * specified character encoding and relativize it against this one,
     * indicating whether {@link Path}s starting with <code>&#2e;&#2e;</code>
     * (parent) are allowed results or not.</p>
     * 
     * @see #relativize(Path, boolean)
     * @param allowParent if {@link Path}s starting with <code>&#2e;&#2e;</code>
     *                    (parent) are allowed results or not.
     * @return a <b>non-null</b> {@link Path} instance.
     */
    public Path relativize(String path, String encoding, boolean allowParent)
    throws UnsupportedEncodingException {
        if (encoding == null) encoding = DEFAULT_ENCODING;
        return this.relativize(parse(path, encoding), allowParent);
    }

    /**
     * <p>Relativize the the specified {@link Path} against this one.</p>
     * 
     * <p>This method is equivalent to a call to
     * {@link #relativize(Path, boolean) relativize(path, true)}.</p>
     * 
     * @see #relativize(Path, boolean)
     * @return a <b>non-null</b> {@link Path} instance.
     */
    public Path relativize(Path path) {
        return this.relativize(path, true);
    }

    /**
     * <p>Relativize the the specified {@link Path} against this one,
     * indicating whether {@link Path}s starting with <code>&#2e;&#2e;</code>
     * (parent) are allowed results or not..</p>
     * 
     * <p>Relativization means (in layman terms) &quot;<i>find the relative
     * path that when resolved against this instance will return the path
     * I am specifying</i>&quot;.</p>
     * 
     * <p>Example: if this {@link Path} is &code;<code>/a/b/xy</code>&code; and
     * the specified {@link Path} is &quot;<code>/a/z/hello</code>&quit;, the
     * returned {@link Path} will be &quot;<code>../z/hello</code>&quot;.</p>
     * 
     * <p>This method will return a new relative {@link Path} only if both this
     * and the specified {@link Path} are absolute. In any other case this
     * method will always return the specified {@link Path} instance.</p>
     * 
     * <p>If this {@link Path} and the specified one are point to the same
     * location, this method will return an empty {@link Path} structure.</p>
     * 
     * @param allowParent if {@link Path}s starting with <code>&#2e;&#2e;</code>
     *                    (parent) are allowed results or not.
     * @return a <b>non-null</b> {@link Path} instance.
     */
    public Path relativize(Path path, boolean allowParent) {
        if (path == null) return new Path(new ArrayList(), false);

        /*
         * If we are relative and the specified path is absolute,
         * there is nothing we can do but return the specified path.
         */
        if ((!this.absolute) && (path.absolute)) return path;
        
        /*
         * If the specified path is relative, we simplify the whole thing by
         * resolving it against this instance. In this case we are guaranteed
         * that up to the diverging point both us and the target path start
         * with the same set of elements.
         */
        if (! path.absolute) path = this.resolve(path);

        /*
         * Now we have only two cases left: either this path and the specified
         * one are both absolute, or they are both relative, and they both
         * start with the same number of elements (somehow) until they diverge.
         * We start by finding out the max number of paths we should examine
         */
        final int thisNum = this.paths.length;
        final int pathNum = path.paths.length;
        final int num  = (thisNum < pathNum ? thisNum : pathNum);

        /* Process the two paths to check for all the common elements */
        int skip = 0;
        while (skip < num) {
            if (path.paths[skip].equals(this.paths[skip])) skip ++;
            else break;
        }

        /*
         * The "skip" variable contains the number of common elements found
         * up to the last one (so, we might return an empty path).
         * If the path would start with with ".." we might return here.
         */
        if ((! allowParent) && (skip + 1 < thisNum)) return path;

        /* Recreate the path to return by adding ".." and the paths */
        final List elems = new ArrayList();
        for (int x = skip + 1; x < thisNum; x ++) {
            elems.add(new PathElement(".."));
        }

        /* Add all the remaining paths to the returned path */
        elems.addAll(path.subList(skip, path.size()));
        return new Path(elems, false);
    }

    /* ====================================================================== */
    /* PUBLIC EXPOSED METHODS                                                 */
    /* ====================================================================== */

    /**
     * <p>Return the {@link PathElement} instance at the specified index.</p>
     */
    public Object get(int index) {
        return this.paths[index];
    }

    /**
     * <p>Return the number of {@link PathElement} instances contained by this
     * {@link Path} instance.</p>
     */
    public int size() {
        return this.paths.length;
    }

    /**
     * <p>Checks if this {@link Path} instance represents an absolute path.</p>
     */
    public boolean isAbsolute() {
        return this.absolute;
    }

    /**
     * <p>Checks if this {@link Path} instance represents a collection.</p>
     */
    public boolean isCollection() {
        return this.collection;
    }

    /**
     * <p>Checks if this {@link Path} is a parent of the specified one.</p>
     */
    public boolean isParent(Path path) {
        final Path relative = this.relativize(path, true);
        if (relative.paths.length == 0) return true;
        return !(relative.absolute || relative.paths[0].getName().equals(".."));
    }

    /* ====================================================================== */
    /* OBJECT METHODS                                                         */
    /* ====================================================================== */

    /**
     * <p>Return the {@link File} representation of this {@link Path}.</p>
     */
    public File toFile() {
        if (this.paths.length == 0) {
            if (this.absolute) return File.listRoots()[0];
            return new File("");
        }
        
        if ((this.paths.length == 1) && (this.collection)) {
            if (this.absolute) return File.listRoots()[0];
            return new File("");
        }

        File file = null;
        if (! this.absolute) file = new File(this.paths[0].getName());
        else {
            final String root = this.paths[0].getName().toLowerCase();
            final File roots[] = File.listRoots();
            for (int x = 0; x < roots.length; x ++) {
                String path = roots[x].getPath().toLowerCase();
                final int pos = path.indexOf(File.separatorChar); 
                if (pos >= 0) path = path.substring(0, pos);
                if (path.equals(root)) {
                    file = roots[x];
                    break;
                }
            }
            if (file == null) {
                file = new File(roots[0], this.paths[0].getName());
            }
        }

        final int end = this.paths.length - (this.collection ? 1 : 0); 
        for (int x = 1; x < end; x++) {
            file = new File(file, this.paths[x].getName());
        }

        return file;
    }

    /**
     * <p>Return the URL-encoded {@link String} representation of this
     * {@link Path} instance.</p>
     */
    public String toString() {
        return this.string;
    }

    /**
     * <p>Return the URL-encoded {@link String} representation of this
     * {@link Path} instance using the specified character encoding.</p>
     */
    public String toString(String encoding)
    throws UnsupportedEncodingException {
        /* Empty string for empty paths */
        if (this.paths.length == 0) return "";
        
        /* One path only (root, current directory, relative entry, ...) */
        if (this.paths.length == 1) {
            final PathElement element = this.paths[0];
            final String string = element.toString(encoding);
            if (this.absolute && this.collection) { // --> root "/;params"
                return "/" + string.substring(1);
            } else if (this.absolute) { // --> root child "/asdf;params"
                return "/" + string;
            } else if (this.collection) { // --> rel current dir ".;params/"
                return string + "/";
            } else { // --> relative current dir entry "abc;xxx"
                return string;
            }
        }

        /* Add all the parent collections followed by a nice / slash */
        StringBuffer buffer = new StringBuffer();
        if (this.absolute) buffer.append('/');
        final int last = this.paths.length - 1;
        for (int x = 0; x < last; x ++) {
            buffer.append(this.paths[x].toString(encoding)).append('/');
        }

        /* Now, let's take a look at the last element */
        final PathElement element = this.paths[last];
        final String string = element.toString(encoding); 
        
        /* If the last element is a collection, we only include the params */
        if (this.collection) {
            return buffer.append(string.substring(1)).toString();
        } else {
            return buffer.append(string).toString();
        }
    }

    /**
     * <p>Return the hash code value of this {@link Path} instance.</p>
     * 
     * <p>The hash code for a {@link Path} instance is equal to the
     * hash code of its {@link #toString() string value}.</p>
     */
    public int hashCode() {
        return this.string.hashCode();
    }

    /**
     * <p>Check if the specified {@link Object} is equal to this
     * {@link Path} instance.</p>
     * 
     * <p>The specified {@link Object} is considered equal to this one if
     * it is <b>non-null</b>, is a {@link Path} instance and all of the
     * {@link PathElement} it contains are equal and in the same order.</p>
     */
    public boolean equals(Object object) {
        if (object == this) return true;
        if (object == null) return false;
        if (object instanceof Path) return super.equals(object);
        return false;
    }
}