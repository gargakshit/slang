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

fun Stack(data) {
	var prev;

	fun execute(op, arg) {
		if (op == "set-prev") {
			prev = arg;
			return nil;
		}

		if (op == "push") {
			var node = Stack(arg("get-second", nil));
			node("set-prev", arg("get-first", nil));
			return node;
		}

		if (op == "peek") {
			return data;
		}

		if (op == "pop") {
			return prev;
		}
	}

	return execute;
}

fun isNumber(string) {
	return str_to_num(string) != nil;
}

var code = "5 dup * dup + . 5 4 swap > ! .";
var spaces = str_count(code, " ");
var stack = Stack(nil);

fun bin_op(fn) {
	var first = stack("peek", nil);
	stack = stack("pop", nil);

	var second = stack("peek", nil);
	stack = stack("pop", nil);

	stack = stack("push", Tuple2(stack, fn(first, second)));
}

fun exec(token) {
	if (isNumber(token)) {
		stack = stack("push", Tuple2(stack, str_to_num(token)));
	} else if (token == "dup") {
		stack = stack("push", Tuple2(stack, stack("peek", nil)));
	} else if (token == "*") {
		fun op(a, b) { return a * b; }
		bin_op(op);
	} else if (token == "+") {
		fun op(a, b) { return a + b; }
		bin_op(op);
	} else if (token == "-") {
		fun op(a, b) { return a - b; }
		bin_op(op);
	} else if (token == "/") {
		fun op(a, b) { return a / b; }
		bin_op(op);
	} else if (token == "<") {
		fun op(a, b) { return a < b; }
		bin_op(op);
	} else if (token == "<=") {
		fun op(a, b) { return a <= b; }
		bin_op(op);
	} else if (token == ">") {
		fun op(a, b) { return a > b; }
		bin_op(op);
	} else if (token == ">=") {
		fun op(a, b) { return a >= b; }
		bin_op(op);
	} else if (token == ".") {
		print stack("peek", nil);
		stack = stack("pop", nil);
	} else if (token == "!") {
		var first = stack("peek", nil);
		stack = stack("pop", nil);
		stack = stack("push", Tuple2(stack, !first));
	} else if (token == "swap") {
		var first = stack("peek", nil);
		stack = stack("pop", nil);

		var second = stack("peek", nil);
		stack = stack("pop", nil);

		stack = stack("push", Tuple2(stack, first));
		stack = stack("push", Tuple2(stack, second));
	}
}

for (var i = 0; i <= spaces; i = i + 1) {
	var token = str_part(code, " ", i);
	exec(token);
}
