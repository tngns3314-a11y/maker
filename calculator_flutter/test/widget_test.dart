import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:calculator_flutter/main.dart';

void main() {
  testWidgets('Adding two numbers shows the correct result', (WidgetTester tester) async {
    await tester.pumpWidget(const CalculatorApp());

    await tester.tap(find.widgetWithText(ElevatedButton, '1'));
    await tester.tap(find.widgetWithText(ElevatedButton, '2'));
    await tester.tap(find.widgetWithText(ElevatedButton, '+'));
    await tester.tap(find.widgetWithText(ElevatedButton, '3'));
    await tester.tap(find.widgetWithText(ElevatedButton, '='));
    await tester.pump();

    expect(find.text('15'), findsOneWidget);
  });
}
