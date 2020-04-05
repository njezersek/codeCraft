Blockly.Blocks['move'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("move")
        .appendField(new Blockly.FieldDropdown([["forward","forward"], ["backward","backward"], ["left","left"], ["right","right"]]), "direction")
        .appendField("1 block");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(210);
 this.setTooltip("Move the robot one block forward.");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['turn'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("turn")
        .appendField(new Blockly.FieldDropdown([["right","right"], ["left","left"], ["around","around"]]), "direction");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(210);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['jump'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("jump");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(210);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['break'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("break a block")
        .appendField(new Blockly.FieldDropdown([["below","below"], ["above","above"], ["in front","front"], ["behind","behind"], ["on the right","right"], ["on the left","left"]]), "direction");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(210);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['set_tool'] = {
  init: function() {
    this.appendValueInput("slot")
        .setCheck("Number")
        .appendField("grab item in slot");
    this.setInputsInline(true);
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(210);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['inventory_index'] = {
  init: function() {
    this.appendValueInput("item")
        .setCheck("String")
        .appendField("index of");
    this.setOutput(true, null);
    this.setColour(210);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField(new Blockly.FieldDropdown([[{"src":"https://minecraftitemids.com/item/32/grass_block.png","width":16,"height":16,"alt":"grass_block"},"grass_block"], [{"src":"https://minecraftitemids.com/item/32/cobblestone.png","width":16,"height":16,"alt":"cobblestone"},"cobblestone"], [{"src":"https://minecraftitemids.com/item/32/stone.png","width":16,"height":16,"alt":"stone"},"stone"]]), "NAME");
    this.setOutput(true, "String");
    this.setColour(210);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['print'] = {
  init: function() {
    this.appendValueInput("s")
        .setCheck(null)
        .appendField("print");
    this.setInputsInline(true);
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(165);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['place'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("place block");
    this.appendValueInput("slot")
        .setCheck("Number")
        .appendField("from slot");
    this.appendDummyInput()
        .appendField("in location")
        .appendField(new Blockly.FieldDropdown([["below","below"], ["above","above"], ["in front","front"], ["behind","behind"], ["on the right","right"], ["on the left","left"]]), "directioin")
        .appendField("me");
    this.appendDummyInput()
        .appendField("in direction")
        .appendField(new Blockly.FieldDropdown([["front-up","up"], ["front-down","down"], ["left","left"], ["right","right"], ["on the right","right"], ["on the left","left"]]), "block_directioin");
    this.setInputsInline(false);
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(210);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['pick'] = {
  init: function() {
    this.appendValueInput("item")
        .setCheck("String")
        .appendField("pick up");
    this.appendValueInput("slot")
        .setCheck("Number")
        .appendField("and put in slot");
    this.setInputsInline(true);
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(210);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['swap'] = {
  init: function() {
    this.appendValueInput("slot1")
        .setCheck("Number")
        .appendField("swap");
    this.appendValueInput("slot2")
        .setCheck("Number")
        .appendField("with");
    this.setInputsInline(true);
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(210);
 this.setTooltip("Swap two items in specified slots in inventory");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['pick_any'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("pick up anything and put in slot");
    this.appendValueInput("slot")
        .setCheck("Number");
    this.setInputsInline(true);
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(210);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['free_slot'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("free slot");
    this.setOutput(true, null);
    this.setColour(210);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['drop'] = {
  init: function() {
    this.appendValueInput("n")
        .setCheck("Number")
        .appendField("drop");
    this.appendValueInput("slot")
        .setCheck("Number")
        .appendField("items in slot");
    this.setInputsInline(true);
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(210);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['drop_all'] = {
  init: function() {
    this.appendValueInput("slot")
        .setCheck("Number")
        .appendField("drop all items in slot");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(210);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['text_style'] = {
  init: function() {
    this.appendValueInput("style")
        .setCheck(null)
        .appendField(new Blockly.FieldDropdown([["red","red"], ["blue","blue"], ["yellow","yellow"]]), "style");
    this.setOutput(true, null);
    this.setColour(165);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['detect_block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("block")
        .appendField(new Blockly.FieldDropdown([["below","below"], ["above","above"], ["in front","front"], ["behind","behind"], ["on the right","right"], ["on the left","left"]]), "directioin");
    this.setOutput(true, null);
    this.setColour(230);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['position'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("position")
        .appendField(new Blockly.FieldDropdown([["x","x"], ["y","y"], ["z","z"]]), "coordinate");
    this.setOutput(true, null);
    this.setColour(230);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['get_block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("block type at");
    this.appendValueInput("x")
        .setCheck("Number")
        .appendField("x");
    this.appendValueInput("y")
        .setCheck("Number")
        .appendField("y");
    this.appendValueInput("z")
        .setCheck("Number")
        .appendField("z");
    this.setOutput(true, null);
    this.setColour(230);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['get_direction'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("direction");
    this.setOutput(true, null);
    this.setColour(230);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};