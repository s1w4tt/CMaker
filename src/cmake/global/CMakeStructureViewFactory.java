package cmake.global;

import cmake.filetypes.CMakeFile;
import cmake.icons.CMakeIcons;
import cmake.psi.*;
import com.intellij.ide.structureView.*;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class builds the presentation of the cmake file
 * that contains function/macro definitions, variable
 * bindings and libraries/targets built.
 * Registered at plugin.xml
 */
public class CMakeStructureViewFactory implements PsiStructureViewFactory {
    @Nullable
    @Override
    public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
        return new TreeBasedStructureViewBuilder() {
            /**
             * @doc Method creates representation for the CMake file 
             * @param editor
             * @return Structure view model that represents CMake file
             */
            @NotNull
            @Override
            public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                return new CMakeViewModel(psiFile);
            }

            /**
             * Determines if to show root node. Show the the CMake root node
             * @return
             */
            @Override
            public boolean isRootNodeShown() {
                return true;
            }
        };
    }

    /**
     * Class represents the model for CMake files
     */
    public static class CMakeViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {
        public CMakeViewModel(@NotNull final PsiFile psiFile) {
            super(psiFile, new CMakeViewElement(psiFile));
            withSuitableClasses(CMakeFile.class);
        }

        @Override
        public boolean isAlwaysShowsPlus(StructureViewTreeElement structureViewTreeElement) {
            return false;
        }

        @Override
        public boolean isAlwaysLeaf(StructureViewTreeElement structureViewTreeElement) {
            return false;
        }
    }

    /**
     * Class represents the item of the CMake file structure model.
     * Created from the PsiElement of the tree. This is wrapper around
     * the element with the presentation(icon,location,text) and navigation functionality
     */
    public static class CMakeViewElement implements StructureViewTreeElement, ItemPresentation, NavigationItem {
        private final PsiElement myElement;

        public CMakeViewElement(PsiElement element) {
            this.myElement = element;
        }
        
        @Override
        public Object getValue() {
            return myElement;
        }

        // ItemPresentation part
        @Nullable
        @Override
        public String getPresentableText() {
            if(myElement instanceof CMakeCompoundExpr)
                return myElement.getFirstChild().getText();
            else if(myElement instanceof CMakeFile)
                return myElement.getContainingFile().getName();
            else
                return null;
        }

        @Nullable
        @Override
        public String getLocationString() {
            return myElement.getProject().getName();
        }

        @Nullable
        @Override
        public Icon getIcon(boolean b) {
            if(myElement instanceof CMakeFile)
                return CMakeIcons.FILE;
            else if(myElement instanceof CMakeCompoundExpr) {
                if (myElement.getFirstChild().toString().matches("function"))
                    return CMakeIcons.FUN;
                else
                    return CMakeIcons.MACRO;
            }
            else
                return null;
        }

        @Nullable
        @Override
        public String getName() {
            return myElement.getText();
        }
        // Navigation part
        @Override
        public void navigate(boolean b) {
            if (myElement instanceof NavigationItem) {
                ((NavigationItem) myElement).navigate(b);
            }
        }

        @Override
        public boolean canNavigate() {
            return myElement instanceof NavigationItem &&
                    ((NavigationItem)myElement).canNavigate();
        }

        @Override
        public boolean canNavigateToSource() {
            return myElement instanceof NavigationItem &&
                    ((NavigationItem)myElement).canNavigateToSource();
        }

        @NotNull
        @Override
        public ItemPresentation getPresentation() {
            return this;
            //return myElement instanceof NavigationItem ?
            //        ((NavigationItem) myElement).getPresentation() : null;
        }

        /**
         * Method carves the contents from the file and
         * creates structure view elements for display
         * @return
         */
        @NotNull
        @Override
        public TreeElement[] getChildren() {
            // Do it only for the CMake file root element
            if (myElement instanceof CMakeFile) {
                final List<TreeElement> treeElements = new ArrayList<TreeElement>();
                // Carve compound expressions that represent function definitions
                // cmake_file
                // |-file_element
                //   |-block
                //     |-compound_expr
                //       |-fbegin <<function(arguments)>>
                CMakeFileElement[] elements = PsiTreeUtil.getChildrenOfType(myElement, CMakeFileElement.class);
                if(null != elements) {
                    // Scan elements for definition blocks
                    for (CMakeFileElement e : elements) {
                        if (e.getFirstChild() instanceof CMakeBlock) {
                            treeElements.add(new CMakeViewElement(e.getBlock().getCompoundExpr()));
                        }
                    }
                }
                return treeElements.toArray(new TreeElement[treeElements.size()]);
            } else {
                return EMPTY_ARRAY;
            }
        }
    }
}
