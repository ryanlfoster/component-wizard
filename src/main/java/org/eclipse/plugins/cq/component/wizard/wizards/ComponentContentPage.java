package org.eclipse.plugins.cq.component.wizard.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import org.eclipse.plugins.cq.component.wizard.Activator;
import org.eclipse.plugins.cq.component.wizard.preferences.PreferenceConstants;

public class ComponentContentPage extends WizardPage {
	
	private Text containerText;
	private Text fileText;
	private Text subFolder;
	private Text description;
	private ISelection selection;

	/**
	 * Constructor for SampleNewWizardPage.
	 *
	 * @param pageName
	 */
	public ComponentContentPage(ISelection selection) {
		super("wizardPage");

		setTitle("CQ5 Component Wizard");
		setDescription("This wizard creates an empty component structure");
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;

		/* first line */
		/* 1 */
		Label label = new Label(container, SWT.NULL);
		label.setText("Project :");
		/* 2 */
		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		/* 3 */
		Button button = new Button(container, SWT.PUSH);
		button.setText("Select");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		/* second line */
		/* 1 */
		label = new Label(container, SWT.NULL);
		label.setText("Component name:");
		/* 2 */
		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		/* 3 */
		label = new Label(container, SWT.NULL);
		label.setText("");

		/* third line */
		/* 1 */
		label = new Label(container, SWT.NULL);
		label.setText("Folder/group name:");
		/* 2 */
		subFolder = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		subFolder.setLayoutData(gd);
		/* 3 */
		label = new Label(container, SWT.NULL);
		label.setText("");


		/* forth line */
		/* 1 */
		label = new Label(container, SWT.NULL);
		label.setText("Component Description:");
		/* 2 */
		description = new Text(container, SWT.BORDER | SWT.MULTI);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		description.setLayoutData(gd);
		/* 3 */
		label = new Label(container, SWT.NULL);
		label.setText("");

		/***/
		initialize();
		dialogChanged();

		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 * @throws CoreException
	 */

	private void initialize() {

		if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;

			if (ssel.size() > 1) {
				return;
			}

			Object obj = ssel.getFirstElement();

			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer) {
					container = (IContainer) obj;
				} else {
					container = ((IResource) obj).getParent();
				}
				containerText.setText(container.getFullPath().toString());
			} else {
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IResource resource = root.findMember(new Path(Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_PROJECT_NAME)));
				if (null != resource) {
					containerText.setText(resource.getFullPath().toString());
				}
			}
		}

		fileText.setText("New Component Name");
		subFolder.setText("general");
		description.setText("Component description");
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), false, "Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				containerText.setText(((Path) result[0]).toString());
			}
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */
	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(getContainerName()));
		String fileName = getFileName();

		if (getContainerName().length() == 0) {
			updateStatus("Project name must be specified");
			return;
		}
		if (container == null || (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus("File container must exist");
			return;
		}
		if (!container.isAccessible()) {
			updateStatus("Project must be writable");
			return;
		}
		if (fileName.length() == 0) {
			updateStatus("Component name must be specified");
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("Component name must be valid");
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return containerText.getText();
	}

	public String getFileName() {
		return fileText.getText();
	}

	public String getSubFolder() {
		return subFolder.getText();
	}
	public String getDescription() {
		return description.getText();
	}
}