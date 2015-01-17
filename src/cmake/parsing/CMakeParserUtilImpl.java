package cmake.parsing;

import cmake.filetypes.CMakeFile;
import cmake.filetypes.CMakeFileType;
import cmake.icons.CMakeIcons;
import cmake.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PairProcessor;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by alex on 1/6/15.
 */
public class CMakeParserUtilImpl {
    public static String getName(CMakeCommandName element) {
        return element.getText();
    }

    public static PsiElement setName(CMakeCommandName element, String newName) {
        ASTNode commandNameNode = element.getFirstChild().getNode();
//        if (commandNameNode != null) {
//            CMakeCommandName newNode = CMakeTypes.Factory.createElement(new CMakeN)
//            ASTNode newKeyNode = property.getFirstChild().getNode();
//            element.getNode().replaceChild(commandNameNode, newKeyNode);
//        }
        return element;
    }
    
    public static PsiElement[] getCommandDefinitions(Project project, String name)
    {
        List<PsiElement> definitions = new ArrayList<PsiElement>();
        
        return (PsiElement[]) definitions.toArray();
    }

    public static class CMakeNamedElementFactory {
//        public static CMake createElement(Project project, String name) {
//            final CMakeFile file = createFile(project, name);
//            return (SimpleProperty) file.getFirstChild();
//        }

        public static CMakeFile createFile(Project project, String text) {
            String name = "cmake.dummy";
            return (CMakeFile) PsiFileFactory.getInstance(project).
                    createFileFromText(name, CMakeFileType.INSTANCE, text);
        }
    }

    public static PsiElement getNameIdentifier(CMakeCommandName element) {
        ASTNode keyNode = element.getNode();
        if (keyNode != null) {
            return keyNode.getPsi();
        } else {
            return null;
        }
    }

    public static ItemPresentation getPresentation(final CMakeBlock element) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return element.getCompoundExpr().getFirstChild().getText();
            }

            @Nullable
            @Override
            public String getLocationString() {
                return element.getContainingFile().getName();
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return CMakeIcons.FILE;
            }
        };
    }

    public static List<PsiElement> getDefinedSymbols(Project project) {
        List<PsiElement> result = new ArrayList<PsiElement>();
        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, 
                CMakeFileType.INSTANCE,
                GlobalSearchScope.allScope(project));
        // Scan each file for named entities
        for (VirtualFile virtualFile : virtualFiles) {
            CMakeFile cmakeFile = (CMakeFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (cmakeFile != null) {
                for( PsiElement e : cmakeFile.getChildren() ) {
                    // Use visitor to visit nodes since the structure is deep
                    PsiElement[] blocks = PsiTreeUtil.getChildrenOfType(e, CMakeBlock.class);
                    if (blocks != null) {
                        Collections.addAll(result, blocks);
                    }
                }
            }
        }
        return result;
    }

    public static List<PsiElement> getDefinedSymbols(Project project, String name) {
        List<PsiElement> result = new ArrayList<PsiElement>();
        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME,
                CMakeFileType.INSTANCE,
                GlobalSearchScope.allScope(project));
        // Scan each file for named entities
        for (VirtualFile virtualFile : virtualFiles) {
            CMakeFile cmakeFile = (CMakeFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (cmakeFile != null) {
                for( PsiElement e : cmakeFile.getChildren() ) {
                    // Use visitor to visit nodes since the structure is deep
                    PsiElement[] blocks = PsiTreeUtil.getChildrenOfType(e, CMakeBlock.class);
                    for( PsiElement b : blocks) {
                        if (b.getFirstChild().getFirstChild().getText().matches(name)) {
                            /*
                            block
                            |
                            ---- compound_expr 
                                 |
                                 ---- command_expr
                                      |
                                      | - function
                                      | - (
                                      | - arguments
                                      |   |
                                      |   |- argument
                                      |   |- *
                             */
                            Collections.addAll(result, b.getFirstChild().getFirstChild().getFirstChild());
                        }
                    }
                }
            }
        }
        return result;
    }
    
    public static void treeWalk(PsiElement start,PairProcessor<PsiElement,PsiElement> pairProcessor) { 
        //TODO: implement walking the tree
    }
}
