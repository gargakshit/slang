var start = clock();

// Used to pass two parameters to linked list's reduce.
fun Tuple2(first, second) {
	// Although arg is not used, it is here for consistency.
	fun execute(op, arg) {
		if (op == "get-first") {
			return first;
		}

		if (op == "get-second") {
			return second;
		}
	}

	return execute;
}

fun Node(data) {
	var next = nil;

	fun execute(op, arg) {
		if (op == "get-data") {
			return data;
		}

		if (op == "get-next") {
			return next;
		}

		if (op == "push") {
			next = Node(arg);
			return next;
		}

		if (op == "for-each") {
			arg(data);
			var _node = next;

			while (_node != nil) {
				arg(_node("get-data", nil));
				_node = _node("get-next", nil);
			}

			return nil;
		}

		if (op == "map") {
			var _root = Node(arg(data));
			var _mapped = _root;
			var _node = next;

			while (_node != nil) {
				_mapped = _mapped("push", arg(_node("get-data", nil)));
				_node = _node("get-next", nil);
			}

			return _root;
		}

		if (op == "reduce") {
			var accumulator = arg("get-first", nil);
			var fn = arg("get-second", nil);

			// Implemented using for-each!
			fun reduction(_data) {
				accumulator = fn(accumulator, _data);
			}

			execute("for-each", reduction);
			return accumulator;
		}
	}

	return execute;
}

fun _print(data111) { print data111; }
fun add(left, right) { return left + right; }
fun add_1(value) { return value + 1; }

var root = Node(1);
// We won't store pushed nodes.
root("push", 2)("push", 3)("push", 4)("push", 5);

print "for-each";
root("for-each", _print); // Prints 1 2 3 4 5 in their own lines!

print "1 added";
root("map", add_1)("for-each", _print); // Prints 2 3 4 5 6.

print "added";
print root("reduce", Tuple2(0, add)); // Prints 15.

print str(root);
var end = clock();
print end - start;
