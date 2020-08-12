package com.ghintech.location.factory;

import org.adempiere.webui.editor.WEditor;
import org.adempiere.webui.factory.IEditorFactory;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.util.DisplayType;

import com.ghintech.location.VEWLocationEditor;

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
        //tableEditor=false;
        //editor.setTableEditor(tableEditor);
        	
        return editor;
	}
	

}
