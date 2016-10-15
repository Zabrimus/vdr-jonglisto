package vdr.jonglisto.web.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.corelib.components.Tree;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ajax.JavaScriptCallback;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.tree.DefaultTreeModel;
import org.apache.tapestry5.tree.TreeModel;
import org.apache.tapestry5.tree.TreeNode;
import org.slf4j.Logger;

import vdr.jonglisto.lib.model.RecPathSummary;
import vdr.jonglisto.lib.model.RecPathTree;
import vdr.jonglisto.lib.model.RecordingInfo;
import vdr.jonglisto.lib.util.DateTimeUtil;
import vdr.jonglisto.web.model.RecPathTreeAdapter;

@Import(stylesheet = "META-INF/assets/css/RecordingsView.css")
public class Recordings extends BaseComponent {

	public enum Function {
		REVIEW, INFO;
	}

	public enum FormAction {
		DELETE, MOVE, MOVE_SINGLE, RENAME
	}

	private Function function;
	private FormAction formAction;

	@Inject
	Logger log;

	@InjectComponent
	private Zone treeZone;

	@InjectComponent
	private Zone recZone;

	@InjectComponent
	private Zone childZone;

	@InjectComponent
	private Zone recordingInfoZone;
	
	@InjectComponent
	protected Epg epg;
	
	@InjectComponent
	private Tree tree;

	@InjectComponent(value = "recordingGrid")
	private Grid grid;

	@InjectComponent
	private Form recForm;

	@Property
	private String recordingInfoModalId = "recordingInfo";

	@Property
	private TreeNode<RecPathTree> treeNode;

	@Property
	private RecPathTree recPath;

	@Persist
	@Property
	private RecPathTree selectedRecPath;

	private RecPathTree root;

	@Persist
	@Property
	private TreeModel<RecPathTree> treeModel;

	@Persist
	@Property
	private List<RecordingInfo> recordings;

	@Persist
	@Property
	private List<RecPathSummary> summary;

	@Property
	private RecPathSummary pathSummary;

	@Persist
	@Property
	private RecordingInfo recording;

	@Property
	private boolean selectAllRecording;

	@Property
	private boolean hasChilds;

	@Property
	private String renameName;

	@Property
	private String renameFilename;

	@Property
	private String moveFilename;

	@Property
	private String targetDirSingle;

	@Persist
	@Property
	private String targetDir;

	private String fileName;

	private List<String> recordingsToChange;

	void pageReset() {
		componentResources.discardPersistentFieldChanges();
	}

	void setupRender() {
		function = Function.REVIEW;

		updateTree();
		onSelectSubDir(treeModel.getRootNodes().get(0).getId());
	}

	private JavaScriptCallback makeScriptToShowInfoModal() {
		return new JavaScriptCallback() {
			public void run(JavaScriptSupport javascriptSupport) {
				javaScriptSupport.require("dialogmodal").invoke("activate").with(recordingInfoModalId,
						new JSONObject());
			}
		};
	}

	public void onRecPathSelected(String treeNodeId) {
		TreeNode<RecPathTree> node = treeModel.getById(treeNodeId);
		selectedRecPath = node.getValue();

		targetDir = selectedRecPath.getFullPath().replaceAll("~", "/");
		recordings = vdrDataService.getRecordingsInPath(getRecordingUuid(), selectedRecPath.isRoot() ? "" : selectedRecPath.getFullPath());

		summary = new ArrayList<RecPathSummary>();

		if (node.getHasChildren()) {
			for (TreeNode<RecPathTree> rpt : node.getChildren()) {
				RecPathSummary s = vdrDataService.getRecSummary(getRecordingUuid(), rpt.getValue().getFullPath());
				s.setName(rpt.getValue().name);
				s.setNodeId(rpt.getId());
				summary.add(s);
			}
		}

		hasChilds = (selectedRecPath != null) && (selectedRecPath.children != null) && (selectedRecPath.children.size() > 0);

		if (request.isXHR()) {
			ajaxResponseRenderer.addRender(treeZone).addRender(childZone).addRender(recZone);
		}
	}

	@SuppressWarnings("unchecked")
	public void onSelectSubDir(String treeNodeId) {
		tree.getDefaultTreeExpansionModel().markExpanded(treeModel.getById(treeNodeId));
		onRecPathSelected(treeNodeId);
	}

	public String getTotalDuration() {
		if (recordings != null) {
			long result = 0;
			for (RecordingInfo s : recordings) {
				result += s.getDurationLong();
			}

			return DateTimeUtil.toDuration(result);
		} else {
			return null;
		}
	}

	public String getTotalSize() {
		if (recordings != null) {
			long result = 0;
			for (RecordingInfo s : recordings) {
				result += s.getFileSize();
			}

			return String.valueOf(result);
		} else {
			return null;
		}
	}

	public boolean isSelected() {
		return false;
	}

	public void setSelected(boolean checkbox) {
		if (recordingsToChange == null) {
			recordingsToChange = new ArrayList<String>();
		}

		if (checkbox) {
			recordingsToChange.add(fileName);
		}
	}

	public boolean isFunction(Function function) {
		return function == this.function;
	}

	public void onRenameRecording(String file, String name) {
		renameName = name;
		renameFilename = file;

		if (request.isXHR()) {
			ajaxResponseRenderer.addRender(recZone);
		}
	}

	public void onDeleteRecording(String file) {
		vdrDataService.deleteRecording(getRecordingUuid(), file);
		recordings = vdrDataService.getRecordingsInPath(getRecordingUuid(), selectedRecPath.getFullPath());

		if (request.isXHR()) {
			ajaxResponseRenderer.addRender(recZone);
		}
	}

	public void onMoveRecording(String file) {
		targetDirSingle = selectedRecPath.getFullPath().replaceAll("~", "/");
		moveFilename = file;

		if (request.isXHR()) {
			ajaxResponseRenderer.addRender(recZone);
		}
	}

	public void onViewDetails(String file) {
		function = Function.INFO;
		recording = vdrDataService.getRecording(getRecordingUuid(), file);

		epg.showInfoZone();
		
		
		if (request.isXHR()) {
			ajaxResponseRenderer.addCallback(makeScriptToShowInfoModal()).addRender(recordingInfoZone);
		}
		
	}

	public void onMoveRecordings() {
		formAction = FormAction.MOVE;
	}

	public void onDeleteRecordings() {
		formAction = FormAction.DELETE;
	}

	public void onSaveRename() {
		formAction = FormAction.RENAME;
	}

	public void onSaveMove() {
		formAction = FormAction.MOVE_SINGLE;
	}

	public boolean onCancelRename() {
		renameName = renameFilename = null;
		formAction = null;
		return true;
	}

	public boolean onCancelMove() {
		targetDirSingle = moveFilename = null;
		formAction = null;
		return true;
	}

	void onValidateFromRecForm() {
		// INFO: Validierungen können hier vorgenommen werden.
		if (formAction == null) {
			return;
		}

		switch (formAction) {
		case DELETE:
			break;

		case MOVE:
			break;

		case MOVE_SINGLE:
			break;

		case RENAME:
			if ((renameName == null) || (renameFilename == null) || StringUtils.isEmpty(renameName)) {
				recForm.recordError("Der neue Name darf nicht leer sein!");
			}
			break;
		}
	}

	void onSuccess() {
		if (formAction != null) {
			switch (formAction) {
			case DELETE:
				vdrDataService.deleteRecordings(getRecordingUuid(), recordingsToChange);
				recordings = vdrDataService.getRecordingsInPath(getRecordingUuid(), selectedRecPath.getFullPath());
				break;

			case MOVE:
				vdrDataService.moveRecordings(getRecordingUuid(), recordingsToChange, targetDir);
				updateTree();
				onSelectSubDir(treeModel.getRootNodes().get(0).getId());
				break;

			case MOVE_SINGLE:
				vdrDataService.moveRecording(getRecordingUuid(), moveFilename, targetDirSingle);
				moveFilename = null;
				updateTree();
				onSelectSubDir(treeModel.getRootNodes().get(0).getId());
				break;

			case RENAME:
				vdrDataService.renameRecording(getRecordingUuid(), renameFilename, renameName);
				recordings = vdrDataService.getRecordingsInPath(getRecordingUuid(), selectedRecPath.getFullPath());
				renameName = renameFilename = null;
				break;

			}
		}

		formAction = null;

		if (request.isXHR()) {
			ajaxResponseRenderer.addRender(recZone);
		}
	}

	void onFailure() {
		renameName = renameFilename = null;
		formAction = null;

		if (request.isXHR()) {
			ajaxResponseRenderer.addRender(recZone);
		}
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public void onSort(String column) {
		grid.getSortModel().updateSort(column);
		
		BeanComparator beanComparator = new BeanComparator(column);
		Collections.sort(recordings, beanComparator);

		if (request.isXHR()) {
			ajaxResponseRenderer.addRender(recZone);
		}
	}

	public String getFileName() {
		fileName = recording.getFileName();
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getMoveTarget() {
		return targetDir;
	}

	public void setMoveTarget(String target) {
		targetDir = target;
	}

	public boolean renameThis() {
		return recording.getFileName().equals(renameFilename);
	}

	public boolean moveThis() {
		return recording.getFileName().equals(moveFilename);
	}

	public boolean showLinkOnly() {
		return !renameThis() && !moveThis();
	}

	public String getNodeClass() {
		if ((selectedRecPath != null) && recPath.uuid.equals(selectedRecPath.uuid)) {
			return "selected";
		}

		return "";
	}

	public boolean showSyncMapWarning() {
		return configuration.useRecordingSyncMap() && !configuration.isUsingSyncMapConfirmed();
	}
	
	private void updateTree() {		
		// INFO: update and sync are not working as expected. Perhaps i have to organize the database entries myself? I need to read the documentation. 
		// vdrDataService.fullRecSync(getRecordingUuid());
		// vdrDataService.recSync(getRecordingUuid());
		vdrDataService.recUpdate(getRecordingUuid());

		List<String> directories = vdrDataService.getRecDirectories(getRecordingUuid());

		root = new RecPathTree(null, "Video");

		for (String path : directories) {
			root.addChild(path);
		}

		ValueEncoder<RecPathTree> recPathEncoder = new ValueEncoder<RecPathTree>() {
			public String toClient(RecPathTree stuff) {
				return stuff.uuid;
			}

			public RecPathTree toValue(String uuid) {
				return root.searchSubTree(uuid);
			}
		};

		treeModel = new DefaultTreeModel<RecPathTree>(recPathEncoder, new RecPathTreeAdapter(false), root);
	}
}