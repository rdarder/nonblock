/*
 * autor: Mauro Monti
 * correo: mauro.monti@globant.com
 */
var data = require('data');
var sqlite3 = require('sqlite3').verbose();

// = 
var database = null;

// =
var node = {
	type: null,
	name: null,
	parent: null,
	clients: null
};

// =  		
function createNode(pRow, pParent) {
	var currentNode = Object.create(node);
	currentNode.type = pRow.TYPE;
	currentNode.name = pRow.NAME;
	currentNode.parent = pParent;
	currentNode.clients = new data.Hash();
	
	return currentNode;
}

// = 
var getQuery = function(pRow) {
	var id = ' IS NULL;'
	if (pRow !== undefined && pRow != null) {
		id = ' = ' + pRow.ID;
	}
	return "SELECT * FROM geo WHERE parent_id" + id;
}

/*
 * 
 */
var generateTreeNode = function(pCallback) {

	// = We store our generated tree here. 
	var tree_node = [];
		
	// = 
	database.parallelize(function() {
	
		// = Recursive function to generate the TreeNode.
		var solveTree = function(pRow, pParent) {

			// = 
			var query = getQuery(pRow);
		
			// = Execute the Query.
			database.all(query, function(pError, pCurrentRows) {
	
				pCurrentRows.forEach(function(pCurrentRow) {
					// = Create a new Node Object.
					var currentNode = createNode(pCurrentRow, pParent);
					
					// = If the currentNode is a leaf (mesa) store it.
					if (currentNode.type.toLowerCase() ===  'mesa') {
						tree_node.push(currentNode);
					}
					
					// = 
					solveTree(pCurrentRow, currentNode);			
				});
			});		
		};
	
		// = First time call.
		solveTree(null, null);
	});
	
	// = 
	database.close(function() {
		// = execute callback to inform that our tree is generated.
		pCallback(tree_node);	
	});

};

/*
 * 
 */
var indexNodes = indexNodes = function(pTreeNodes, pEndCallback) {
	if (pTreeNodes == null && pTreeNodes === undefined) {
		return;
	}
	
	// = 
	var indexed_nodes = new data.Hash();
	
	// =	
	var index = function(pCurrentNode, pCallback) {
		if (pCurrentNode != null && pCurrentNode !== undefined) {
			var key_type = pCurrentNode.type;
			
			// = Check for an existent key. 
			var indexed_value = indexed_nodes.get(key_type);

			// = if an existent values?
			if (indexed_value != null && indexed_value !== undefined) {
				indexed_value.set(pCurrentNode.name, pCurrentNode);
				indexed_nodes.set(key_type, indexed_value);

			} else {
				// = is a new values.
				var new_value = new data.Hash();
				new_value.set(pCurrentNode.name, pCurrentNode);

				// = Index
				indexed_nodes.set(key_type, new_value);			
			}
			
			// = Recursive Call.
			index(pCurrentNode.parent, pCallback);
			
		} else {
			// = Current path was indexed.
			pCallback();
		}
	}
	
	// = 
	var count_indexed_paths = pTreeNodes.length;
	
	pTreeNodes.forEach(function(pCurrentNode) {
		index(pCurrentNode, function(){
			if (--count_indexed_paths == 0) {
				pEndCallback(indexed_nodes);
			}
		});
	});
	
};

// = 
var tree_node = [];

// = 
var indexed_nodes = null;

/*
 * 
 */
exports.getTreeNode = getTreeNode = function() {
	return tree_node;
}

/*
 * 
 */
exports.getIndexedNodes = getIndexedNodes = function() {
	return indexed_nodes;
}
	
/*
 * 
 */
exports.initialize = initialize = function(pConfiguration, pCallback) {
	var folder = pConfiguration.database.folder;
	var name = pConfiguration.database.name;
	
	// = Open Database
	database = new sqlite3.Database((folder + name));
	
	// = Return Object.
	var structure = Object.create({treeLeafs: null, indexedTree: null});
	
	// =
	var init_tree_generation = function(pCallback) {
		generateTreeNode(pCallback);
	};

	// =	
	var init_tree_indexation = function(pTreeNodes, pCallback) {
		structure.treeLeafs = pTreeNodes;
		
		indexNodes(pTreeNodes, pCallback);
	};

	// =	
	var init = function(pCallback) {
		init_tree_generation(function(pTreeNodes){
			console.info("Tree Generation Done!.");
			init_tree_indexation(pTreeNodes, pCallback);
		})	
	};
	
	// = Initialize Data.
	init(function(pIndexedNodes){
		structure.indexedNodes = pIndexedNodes;
		
		console.info("Tree Indexation Done!.");
		
		// =
		pCallback(structure);
	});
}