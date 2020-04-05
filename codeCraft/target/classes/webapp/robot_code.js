Blockly.JavaScript['move'] = function(block) {
  var dropdown_direction = block.getFieldValue('direction');
  var code = 'mc.move("'+dropdown_direction+'");\n';
  return code;
};

Blockly.JavaScript['turn'] = function(block) {
  var dropdown_direction = block.getFieldValue('direction');
  var code = 'mc.turn("'+dropdown_direction+'");\n';
  return code;
};

Blockly.JavaScript['jump'] = function(block) {
  var code = 'mc.jump();\n';
  return code;
};

Blockly.JavaScript['break'] = function(block) {
  var dropdown_direction = block.getFieldValue('direction');
  var code = 'mc.break("'+dropdown_direction+'");\n';
  return code;
};

Blockly.JavaScript['set_tool'] = function(block) {
  var value_slot = Blockly.JavaScript.valueToCode(block, 'slot', Blockly.JavaScript.ORDER_ATOMIC);
  var code = 'mc.setTool('+value_slot+');\n';
  return code;
};

Blockly.JavaScript['inventory_index'] = function(block) {
  var value_item = Blockly.JavaScript.valueToCode(block, 'item', Blockly.JavaScript.ORDER_ATOMIC);
  var code = 'mc.inventoryIndex('+value_item+')';
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['block'] = function(block) {
  var dropdown_name = block.getFieldValue('NAME');
  var code = dropdown_name;
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['print'] = function(block) {
  var value_s = Blockly.JavaScript.valueToCode(block, 's', Blockly.JavaScript.ORDER_ATOMIC);
  var code = 'mc.print("'+value_s+'");\n';
  return code;
};

Blockly.JavaScript['place'] = function(block) {
  var value_slot = Blockly.JavaScript.valueToCode(block, 'slot', Blockly.JavaScript.ORDER_ATOMIC);
  var dropdown_directioin = block.getFieldValue('directioin');
  var dropdown_block_directioin = block.getFieldValue('block_directioin');
  var code = 'mc.place("'+dropdown_directioin+'", "'+dropdown_block_directioin+'");\n';
  return code;
};

Blockly.JavaScript['pick'] = function(block) {
  var value_item = Blockly.JavaScript.valueToCode(block, 'item', Blockly.JavaScript.ORDER_ATOMIC);
  var value_slot = Blockly.JavaScript.valueToCode(block, 'slot', Blockly.JavaScript.ORDER_ATOMIC);
  var code = 'mc.pickUp("'+value_item+'", '+value_slot+');\n';
  return code;
};

Blockly.JavaScript['swap'] = function(block) {
  var value_slot1 = Blockly.JavaScript.valueToCode(block, 'slot1', Blockly.JavaScript.ORDER_ATOMIC);
  var value_slot2 = Blockly.JavaScript.valueToCode(block, 'slot2', Blockly.JavaScript.ORDER_ATOMIC);
  var code = 'mc.swapInventory('+value_slot1+', '+value_slot2+');\n';
  return code;
};

Blockly.JavaScript['pick_any'] = function(block) {
  var value_slot = Blockly.JavaScript.valueToCode(block, 'slot', Blockly.JavaScript.ORDER_ATOMIC);
  var code = 'mc.pickUpAny('+value_slot+');\n';
  return code;
};

Blockly.JavaScript['free_slot'] = function(block) {
  var code = 'mc.firstFreeSlot()';
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['drop'] = function(block) {
  var value_n = Blockly.JavaScript.valueToCode(block, 'n', Blockly.JavaScript.ORDER_ATOMIC);
  var value_slot = Blockly.JavaScript.valueToCode(block, 'slot', Blockly.JavaScript.ORDER_ATOMIC);
  var code = 'drop('+value_slot+', '+value_n+');\n';
  return code;
};

Blockly.JavaScript['drop_all'] = function(block) {
  var value_slot = Blockly.JavaScript.valueToCode(block, 'slot', Blockly.JavaScript.ORDER_ATOMIC);
  var code = 'dropAll('+value_slot+');\n';
  return code;
};

Blockly.JavaScript['text_style'] = function(block) {
  var dropdown_style = block.getFieldValue('style');
  var value_style = Blockly.JavaScript.valueToCode(block, 'style', Blockly.JavaScript.ORDER_ATOMIC);
  var code = 'mc.textStyle("'+dropdown_style+'")+"'+value_style+'"';
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['detect_block'] = function(block) {
  var dropdown_directioin = block.getFieldValue('directioin');
  var code = 'mc.getBlockType("'+dropdown_directioin+'")';
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['position'] = function(block) {
  var dropdown_coordinate = block.getFieldValue('coordinate');
  var code = 'mc.getCoordinate("+dropdown_coordinate+")';
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['get_block'] = function(block) {
  var value_x = Blockly.JavaScript.valueToCode(block, 'x', Blockly.JavaScript.ORDER_ATOMIC);
  var value_y = Blockly.JavaScript.valueToCode(block, 'y', Blockly.JavaScript.ORDER_ATOMIC);
  var value_z = Blockly.JavaScript.valueToCode(block, 'z', Blockly.JavaScript.ORDER_ATOMIC);
  var code = 'mc.getBlockTypeAt('+value_x+','+value_y+','+value_z+')';
  return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['get_direction'] = function(block) {
  var code = 'mc.getDirection()';
  return [code, Blockly.JavaScript.ORDER_NONE];
};