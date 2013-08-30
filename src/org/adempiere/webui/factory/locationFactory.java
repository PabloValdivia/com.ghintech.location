package org.adempiere.webui.factory;

import org.adempiere.webui.editor.VEWLocationEditor;
import org.adempiere.webui.editor.WEditor;
import org.adempiere.webui.editor.WLocationEditor;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.util.DisplayType;

public class locationFactory implements IEditorFactory  {

	@Override
	public WEditor getEditor(GridTab gridTab, GridField gridField,
			boolean tableEditor) {
		// TODO Auto-generated method stub
		
		if (gridField == null)
        {
            return null;
        }

        WEditor editor = null;
        int displayType = gridField.getDisplayType();

        /** Not a Field */
        if (gridField.isHeading())
        {
            return null;
        }

        if (displayType == DisplayType.Location)
        {
            editor = new VEWLocationEditor(gridField);
        }
        //editor.setTableEditor(tableEditor);
        
        return editor;
	}
	

}
