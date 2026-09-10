def calculate(a, b, op):
    if op == "+":
        return a + b
    elif op == "-":
        return a - b
    elif op == "*":
        return a * b
    elif op == "/":
        if b == 0:
            print("Error: Cannot divide by zero.")
            return None
        return a / b
    else:
        return None


def main():
    print("Simple Calculator")
    print("Operators: +  -  *  /   (enter q to quit)")

    while True:
        op = input("\nOperator: ")
        if op == "q":
            print("Bye!")
            break

        if op not in ["+", "-", "*", "/"]:
            print("Invalid operator, try again.")
            continue

        try:
            a = float(input("First number: "))
            b = float(input("Second number: "))
        except ValueError:
            print("Please enter valid numbers.")
            continue

        result = calculate(a, b, op)
        if result is not None:
            print("Result =", result)


main()
