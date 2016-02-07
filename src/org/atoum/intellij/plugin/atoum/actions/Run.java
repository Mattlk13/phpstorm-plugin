package org.atoum.intellij.plugin.atoum.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.atoum.intellij.plugin.atoum.model.RunnerConfiguration;
import org.atoum.intellij.plugin.atoum.run.Runner;
import org.jetbrains.annotations.Nullable;
import org.atoum.intellij.plugin.atoum.Utils;

public class Run extends AnAction {

    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(false);
        event.getPresentation().setVisible(true);
        event.getPresentation().setText("atoum - run test");

        PhpClass currentTestClass = getCurrentTestClass(event);
        if (currentTestClass != null) {
            event.getPresentation().setText("atoum - run test : " + currentTestClass.getName());
            event.getPresentation().setEnabled(true);
        } else {
            VirtualFile selectedDir = getCurrentTestDirectory(event);
            if (selectedDir != null) {
                event.getPresentation().setText("atoum - run dir");
                event.getPresentation().setEnabled(true);
            }
        }
    }

    protected void saveFiles(PhpClass currentTestClass, Project project) {
        Document documentTestClass = FileDocumentManager.getInstance().getDocument(currentTestClass.getContainingFile().getVirtualFile());
        Document documentTestedClass = FileDocumentManager.getInstance().getDocument(Utils.locateTestedClass(project, currentTestClass).getContainingFile().getVirtualFile());
        FileDocumentManager.getInstance().saveDocument(documentTestClass);
        FileDocumentManager.getInstance().saveDocument(documentTestedClass);

    }

    public void actionPerformed(final AnActionEvent e) {
        PhpClass currentTestClass = getCurrentTestClass(e);
        VirtualFile selectedDir = null;
        if (currentTestClass == null) {
            selectedDir = getCurrentTestDirectory(e);
            if (null == selectedDir) {
                return;
            }
        }

        Project project = e.getProject();

        RunnerConfiguration runConfiguration = new RunnerConfiguration();
        if (null != currentTestClass) {
            saveFiles(currentTestClass, project);
            runConfiguration.setFile((PhpFile)currentTestClass.getContainingFile());
        }

        if (null != selectedDir) {
            runConfiguration.setDirectory(selectedDir);
        }

        Runner runner = new Runner(project);
        runner.run(runConfiguration);
    }


    @Nullable
    protected VirtualFile getCurrentTestDirectory(AnActionEvent e)
    {
        Object eventVirtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);

        if (null == eventVirtualFile) {
            return null;
        }

        if (!(eventVirtualFile instanceof VirtualFile)) {
            return null;
        }

        VirtualFile virtualFile = ((VirtualFile) eventVirtualFile);

        if (!virtualFile.isDirectory()) {
            return null;
        }

        return virtualFile;
    }

    @Nullable
    protected PhpClass getCurrentTestClass(AnActionEvent e) {
        Object psiFile = e.getData(PlatformDataKeys.PSI_FILE);

        if (null == psiFile) {
            return null;
        }

        if (!(psiFile instanceof PhpFile)) {
            return null;
        }
        PhpFile phpFile = ((PhpFile) psiFile);

        PhpClass currentClass = Utils.getFirstClassFromFile(phpFile);
        if (null == currentClass) {
            return null;
        }

        if (!Utils.isClassAtoumTest(currentClass)) {
            return Utils.locateTestClass(e.getProject(), currentClass);
        }

        return currentClass;
    }
}
