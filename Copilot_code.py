def get_number(prompt):
    while True:
        try:
            return float(input(prompt))
        except ValueError:
            print("Invalid input. Please enter a numeric value.")


def calculate(a, b, op):
    if op == "+":
        return a + b
    elif op == "-":
        return a - b
    elif op == "*":
        return a * b
    elif op == "/":
        if b == 0:
            raise ZeroDivisionError("Cannot divide by zero.")
        return a / b
    else:
        raise ValueError(f"Unsupported operator '{op}'.")


def main():
    print("Simple Calculator (type 'q' to quit)")
    while True:
        op = input("\nEnter operator (+, -, *, /) or 'q' to quit: ").strip()
        if op.lower() == "q":
            print("Goodbye!")
            break
        if op not in ("+", "-", "*", "/"):
            print("Invalid operator. Choose from +, -, *, /.")
            continue

        a = get_number("Enter first number: ")
        b = get_number("Enter second number: ")

        try:
            result = calculate(a, b, op)
            print(f"Result: {a} {op} {b} = {result}")
        except ZeroDivisionError as e:
            print(f"Error: {e}")
        except ValueError as e:
            print(f"Error: {e}")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\nCalculator interrupted. Exiting.")
