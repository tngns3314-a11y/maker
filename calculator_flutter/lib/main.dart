import 'package:flutter/material.dart';

void main() {
  runApp(const CalculatorApp());
}

class CalculatorApp extends StatelessWidget {
  const CalculatorApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: '더하기 계산기',
      theme: ThemeData(
        brightness: Brightness.dark,
        scaffoldBackgroundColor: Colors.black,
      ),
      home: const CalculatorScreen(),
    );
  }
}

class CalculatorScreen extends StatefulWidget {
  const CalculatorScreen({super.key});

  @override
  State<CalculatorScreen> createState() => _CalculatorScreenState();
}

class _CalculatorScreenState extends State<CalculatorScreen> {
  String _expression = '';
  String _result = '0';
  bool _justEvaluated = false;

  void _onDigitPressed(String digit) {
    setState(() {
      if (_justEvaluated) {
        _expression = '';
        _justEvaluated = false;
      }
      _expression += digit;
      _result = _expression;
    });
  }

  void _onPlusPressed() {
    setState(() {
      if (_expression.isEmpty || _expression.endsWith('+')) return;
      _justEvaluated = false;
      _expression += '+';
      _result = _expression;
    });
  }

  void _onEqualsPressed() {
    setState(() {
      if (_expression.isEmpty || _expression.endsWith('+')) return;
      final sum = _expression.split('+').map(int.parse).reduce((a, b) => a + b);
      _result = sum.toString();
      _justEvaluated = true;
    });
  }

  void _onClearPressed() {
    setState(() {
      _expression = '';
      _result = '0';
      _justEvaluated = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Column(
          children: [
            Expanded(
              flex: 2,
              child: Container(
                width: double.infinity,
                padding: const EdgeInsets.symmetric(horizontal: 24),
                alignment: Alignment.bottomRight,
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.end,
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      _expression,
                      style: const TextStyle(color: Colors.grey, fontSize: 28),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      _result,
                      key: const Key('result'),
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 56,
                        fontWeight: FontWeight.w300,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ),
              ),
            ),
            Expanded(
              flex: 3,
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  children: [
                    _buttonRow(['C']),
                    _buttonRow(['7', '8', '9']),
                    _buttonRow(['4', '5', '6']),
                    _buttonRow(['1', '2', '3']),
                    _buttonRow(['0', '+', '=']),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buttonRow(List<String> labels) {
    return Expanded(
      child: Row(children: labels.map((label) => _buildButton(label)).toList()),
    );
  }

  Widget _buildButton(String label) {
    final isOperator = label == '+' || label == '=';
    final isClear = label == 'C';
    return Expanded(
      child: Padding(
        padding: const EdgeInsets.all(6),
        child: ElevatedButton(
          style: ElevatedButton.styleFrom(
            backgroundColor: isClear
                ? Colors.redAccent
                : isOperator
                    ? Colors.orange
                    : Colors.grey[850],
            foregroundColor: Colors.white,
            shape: const CircleBorder(),
            padding: EdgeInsets.zero,
            textStyle: const TextStyle(fontSize: 28),
          ),
          onPressed: () {
            if (label == 'C') {
              _onClearPressed();
            } else if (label == '+') {
              _onPlusPressed();
            } else if (label == '=') {
              _onEqualsPressed();
            } else {
              _onDigitPressed(label);
            }
          },
          child: AspectRatio(
            aspectRatio: 1,
            child: Center(child: Text(label)),
          ),
        ),
      ),
    );
  }
}
