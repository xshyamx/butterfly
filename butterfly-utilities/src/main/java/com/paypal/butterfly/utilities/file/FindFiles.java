package com.paypal.butterfly.utilities.file;

import com.paypal.butterfly.extensions.api.TransformationContext;
import com.paypal.butterfly.extensions.api.TransformationUtility;
import com.paypal.butterfly.extensions.api.TUExecutionResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Finds files based on a regular expression
 * against the file name and/or the file path. The search might be
 * recursive (including sub-folders) or not. If a file path regular
 * expression is set, then the search will be automatically recursive.
 * If no file path regular expression is set, then the search
 * is not recursive by default, but it may be set to as well.
 * <br>
 * The root directory from where the search should take place
 * can be defined by {@link #relative(String)},
 * {@link #absolute(String)} or {@link #absolute(String, String)}.
 * If not set explicitly, then the search will happen from the root
 * of the transformed application, which is equivalent to setting
 * {@link #relative(String)} to {@code "."}
 * <br>
 * If no files have been found, an empty list is returned and a
 * warning is stated in the result
 *
 * @author facarvalho
 */
public class FindFiles extends TransformationUtility<FindFiles> {

    private static final String DESCRIPTION = "Find files whose name and/or path match regular expression and are under %s%s";

    private String nameRegex;
    private String pathRegex;
    private boolean recursive;

    public FindFiles() {
    }

    /**
     * Utility to find files based on a regular expression
     * against the file name. The search might be
     * recursive (including sub-folders) or not.
     * <br>
     * The root directory from where the search should take place
     * can be defined by {@link #relative(String)},
     * {@link #absolute(String)} or {@link #absolute(String, String)}.
     * If not set explicitly, then the search will happen from the root
     * of the transformed application, which is equivalent to setting
     * {@link #relative(String)} to {@code "."}
     *
     * @param nameRegex regular expression to be applied against file name during search
     * @param recursive if true, sub-folders will also be searched
     */
    public FindFiles(String nameRegex, boolean recursive) {
        setNameRegex(nameRegex);
        setRecursive(recursive);
    }

    /**
     * Utility to find files based on a regular expression
     * against the file name and the file path. Because a file path regular
     * expression is set, then the search will be automatically and
     * necessarily recursive.
     * <br>
     * <strong>Important notes:</strong>
     * <ul>
     *      <li>Use forward slash as file separator. If the OS
     *      used during transformation execution uses another character
     *      as file separator, that will be automatically converted
     *      by this utility</li>
     *      <li>Setting this to a non null value automatically sets
     *      recursive property to true</li>
     *      <li>This regular expression will be evaluated against
     *      the file path <strong>starting from the search root
     *      directory</strong></li>
     * </ul>
     * <br>
     * The root directory from where the search should take place
     * can be defined by {@link #relative(String)},
     * {@link #absolute(String)} or {@link #absolute(String, String)}.
     * If not set explicitly, then the search will happen from the root
     * of the transformed application, which is equivalent to setting
     * {@link #relative(String)} to {@code "."}
     *
     * @param nameRegex regular expression to be applied against file name during search
     * @param pathRegex regular expression to be applied against file path during search
     */
    public FindFiles(String nameRegex, String pathRegex) {
        setNameRegex(nameRegex);
        setPathRegex(pathRegex);
    }

    /**
     * Set regular expression to be used to match the file name
     * during the search
     *
     * @param nameRegex regular expression to be used to match the file name
     * during the search
     * @return this transformation utility instance
     */
    public FindFiles setNameRegex(String nameRegex) {
        checkForEmptyString("Name regex", nameRegex);
        this.nameRegex = nameRegex;
        return this;
    }

    /**
     * Set regular expression to be used to match the file path
     * during the search<br>
     * <strong>Important notes:</strong>
     * <ul>
     *      <li>Use forward slash as file separator. If the OS
     *      used during transformation execution uses another character
     *      as file separator, that will be automatically converted
     *      by this utility</li>
     *      <li>Setting this to a non null value automatically sets
     *      recursive property to true</li>
     *      <li>This regular expression will be evaluated against
     *      the file path <strong>starting from the search root
     *      directory</strong></li>
     * </ul>
     *
     * @param pathRegex regular expression to be used to match the file path
     * during the search
     * @return this transformation utility instance
     */
    public FindFiles setPathRegex(String pathRegex) {
        checkForEmptyString("Path regex", pathRegex);
        this.pathRegex = pathRegex;
        if (pathRegex != null) {
            recursive = true;
        }
        return this;
    }

    /**
     * Set whether the search should be recursive or not.
     * If a file path regular expression has been set,
     * then this property will be automatically set to
     * true.<br>
     * <strong>Important: setting this to false automatically sets
     * the file path regular expression to null</strong>
     *
     * @param recursive whether the search should be recursive
     * @return this transformation utility instance
     */
    public FindFiles setRecursive(boolean recursive) {
        this.recursive = recursive;
        if (!recursive) {
            pathRegex = null;
        }
        return this;
    }

    /**
     * Returns the file name regular expression
     *
     * @return the file name regular expression
     */
    public String getNameRegex() {
        return nameRegex;
    }

    /**
     * Returns the file path regular expression
     *
     * @return the file path regular expression
     */
    public String getPathRegex() {
        return pathRegex;
    }

    /**
     * Returns whether the file search is recursive or not
     *
     * @return whether the file search is recursive or not
     */
    public boolean isRecursive() {
        return recursive;
    }

    @Override
    public String getDescription() {
        String folder = getRelativePath();
        if (StringUtils.isBlank(folder) || ".".equals(folder)) {
            folder = "the root folder";
        }
        return String.format(DESCRIPTION, folder, (recursive ? " and sub-folders" : " only (not including sub-folders)"));
    }

    @Override
    protected TUExecutionResult execution(File transformedAppFolder, TransformationContext transformationContext) {
        final File searchRootFolder = getAbsoluteFile(transformedAppFolder, transformationContext);

        String _pathRegex = pathRegex;
        if (pathRegex != null && File.separatorChar != '/') {
            _pathRegex = pathRegex.replace('/', File.separatorChar);
        }
        final String normalizedPathRegex = _pathRegex;

        IOFileFilter fileFilter = new AbstractFileFilter() {
            public boolean accept(File file) {
                if (!file.isFile()) {
                    return false;
                }
                if (nameRegex != null && !file.getName().matches(nameRegex)) {
                    return false;
                }
                if (normalizedPathRegex != null) {
                    String relativePath = getRelativePath(searchRootFolder, file.getParentFile());
                    if (!relativePath.matches(normalizedPathRegex)) {
                        return false;
                    }
                }
                return true;
            }
        };
        Collection<File> files = FileUtils.listFiles(searchRootFolder, fileFilter, (recursive ? TrueFileFilter.INSTANCE : null));
        TUExecutionResult result = null;

        if(files.size() == 0) {
            result = TUExecutionResult.warning(this, "No files have been found", new ArrayList<File>(files));
        } else {
            result = TUExecutionResult.value(this, new ArrayList<File>(files));
        }

        return result;
    }

}
