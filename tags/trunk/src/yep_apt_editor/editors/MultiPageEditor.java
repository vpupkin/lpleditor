package yep_apt_editor.editors;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import org.apache.maven.doxia.parser.ParseException;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.ide.IDE;

import yep_apt_editor.Activator;

//
 

public class MultiPageEditor extends MultiPageEditorPart implements
		IResourceChangeListener, IPropertyListener {

	public MultiPageEditor() {
		preferenceStore = Activator.getDefault().getPreferenceStore();
		aptEditorDriver = new APTEditorDriver();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	void createPageEdit() {
		try {
			editor = new TextEditor();
			addPage(1, editor, getEditorInput());
			setPageText(1, "Edit");
			editor.addPropertyListener(this);
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(),
					"Error creating nested text editor", null, e.getStatus());
		}
	}

	void createPageView() {
		browser = new Browser(getContainer(), 0);
		addPage(0, browser);
		setPageText(0, "View");
	}

	protected void createPages() {
		setPartName(getEditorInput().getName());
		createPageView();
		createPageEdit();
		if (getEditorText().length() == 0)
			setActivePage(1);
		else
			try {
				updateView();
				setActivePage(0);
			} catch (ParseException _ex) {
				setActivePage(1);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public void dispose() {
		clearProblemMarkers();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	} 

	public void doSave(IProgressMonitor monitor) {
		getEditor(1).doSave(monitor);
		try {
			updateView();
		} catch (ParseException _ex) {
			setActivePage(1);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void doSaveAs() {
		IEditorPart editor = getEditor(1);
		editor.doSaveAs();
		setPageText(1, editor.getTitle());
		setInput(editor.getEditorInput());
	}

	public void gotoMarker(IMarker marker) {
		setActivePage(1);
		IDE.gotoMarker(getEditor(1), marker);
	}

	public void init(IEditorSite site, IEditorInput editorInput)
			throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput)) {
			throw new PartInitException(
					"Invalid Input: Must be IFileEditorInput");
		} else {
			super.init(site, editorInput);
			return;
		}
	}

	public boolean isSaveAsAllowed() {
		return true;
	}

	protected void pageChange(int newPageIndex) {
		try {
			super.pageChange(newPageIndex);
			IMarker markers[] = (IMarker[]) null;
			try {
				markers = getAPTFile().findMarkers(
						"org.eclipse.core.resources.problemmarker", true, 2);
			} catch (CoreException ce) {
				ce.printStackTrace();
			}
			if (newPageIndex == 0 && isDirty()) {
				boolean okToSave = MessageDialog
						.openConfirm(
								getSite().getShell(),
								"Save required",
								"The file must be saved before switching to the View pane.\nDo you wish to save now?");
				if (okToSave) {
					editor.doSave(null);
					updateView();
				} else {
					setActivePage(1);
				}
			} else if (newPageIndex == 0 && markers != null
					&& markers.length > 0) {
				MessageDialog
						.openError(
								getSite().getShell(),
								"Errors in APT file",
								"Errors exist in the APT file.\nErrors must be fixed prior to switching to View mode.");
				setActivePage(1);
			} else {
				super.pageChange(newPageIndex);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void setActivePage(int pageIndex) {
		super.setActivePage(pageIndex);
	}

	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == 2)
			Display.getDefault().asyncExec(new Runnable() {

				public void run() {
					IWorkbenchPage pages[] = getSite().getWorkbenchWindow()
							.getPages();
					for (int i = 0; i < pages.length; i++)
						if (((FileEditorInput) editor.getEditorInput())
								.getFile().getProject().equals(
										event.getResource())) {
							IEditorPart editorPart = pages[i].findEditor(editor
									.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}

				}

//				final MultiPageEditor this$0;
//				private final IResourceChangeEvent val$event;
//
//				{
//					this$0 = MultiPageEditor.this;
//					event = iresourcechangeevent;
//					super();
//				}
			});
	}

	public void updateView() throws ParseException, CoreException {
		aptEditorDriver.setTOC(preferenceStore.getBoolean("tableOfContent"));
		aptEditorDriver.setSectionsNumbered(preferenceStore
				.getBoolean("sectionNumbering"));
		String cssPath = preferenceStore.getString("cssPath");
		if (cssPath != null && cssPath.length() > 0)
			aptEditorDriver.putPI("html", "css", cssPath);
		String aptFilePath = ((FileEditorInput) editor.getEditorInput())
				.getPath().toOSString();
		try {
			String charset = ((FileEditorInput) editor.getEditorInput())
					.getFile().getCharset();
			if ("MS932".equals(charset))
				charset = "Shift_JIS";
			aptEditorDriver.setEncoding(charset);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		clearProblemMarkers();
		try {
			String htmlFilePath = aptEditorDriver.convert(aptFilePath);
			browser.setUrl(new File( htmlFilePath).getAbsolutePath() );
		} catch (ParseException e) {
			  {
				ParseException pe = (ParseException) e;
				try {
					IMarker marker = getAPTFile().createMarker(
							"org.eclipse.core.resources.problemmarker");
					if (marker.exists()) {
						marker.setAttribute("transient", true);
						marker.setAttribute("lineNumber", pe.getLineNumber());
						marker.setAttribute("message", pe.getMessage());
						marker.setAttribute("severity", 2);
					}
				} catch (CoreException ce) {
					ce.printStackTrace();
				}
				throw pe;
			}
			
		}catch (IllegalArgumentException e) {
 			 e.printStackTrace();
 			 String msg = e.getMessage();
 			msg+=" Try to build project-site to fix it first.";
			IStatus status = new Status(Status.INFO, Activator.getDefault().toString(), Status.OK, msg , e);
			Activator.getDefault().getLog().log(status );
		} catch (PlexusContainerException e) {
			 String msg = e.getMessage();
				IStatus status = new Status(Status.INFO, Activator.getDefault().toString(), Status.OK, msg , e);
				Activator.getDefault().getLog().log(status );
			e.printStackTrace();
		} catch (ComponentLookupException e) {
			 String msg = e.getMessage();
				IStatus status = new Status(Status.INFO, Activator.getDefault().toString(), Status.OK, msg , e);
				Activator.getDefault().getLog().log(status );
			e.printStackTrace();
		} catch (IOException e) {
			 String msg = e.getMessage();
				IStatus status = new Status(Status.INFO, Activator.getDefault().toString(), Status.OK, msg , e);
				Activator.getDefault().getLog().log(status );
			e.printStackTrace();
		}
	}

	
    /**

     * Set error status line of RCP with this message.

     * @param message String to be placed on status line, set to 

     * null to clear status message.  

     */

	
	private String getEditorText() {
		return editor.getDocumentProvider()
				.getDocument(editor.getEditorInput()).get();
	}

	public void propertyChanged(Object source, int propId) {
		if (propId == 260 && (source instanceof TextEditor)) {
			TextEditor te = (TextEditor) source;
			setPartName(te.getPartName());
		}
	}

	private org.eclipse.core.resources.IFile getAPTFile() {
		return ((FileEditorInput) editor.getEditorInput()).getFile();
	}

	private void clearProblemMarkers() {
		try {
			IMarker markers[] = getAPTFile().findMarkers(
					"org.eclipse.core.resources.problemmarker", true, 2);
			IMarker aimarker[] = markers;
			int i = 0;
			for (int j = aimarker.length; i < j; i++) {
				IMarker marker = aimarker[i];
				marker.delete();
			}

		} catch (CoreException ce) {
			ce.printStackTrace();
		}
	}

	private TextEditor editor;
	private Browser browser;
	private static final int PAGE_INDEX_VIEW = 0;
	private static final int PAGE_INDEX_EDITOR = 1;
	IPreferenceStore preferenceStore;
	APTEditorDriver aptEditorDriver;

}
