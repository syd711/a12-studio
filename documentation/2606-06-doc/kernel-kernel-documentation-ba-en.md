# kernel kernel documentation ba en

Kernel Language
Table of contents
1. Preface . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 4
1.1. Kernel Language . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 4
1.2. Who should read this document? . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
1.3. Model examples . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
2. Models . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
2.1. Model structure. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
2.2. Paths. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
2.2.1. Absolute paths . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
2.2.2. Relative paths. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
2.3. Constants and data types . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 11
2.4. Field types . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 12
2.4.1. String . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 14
2.4.2. Number . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 15
2.4.3. Date, Time, DateTime . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
2.4.4. DateRange. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
2.4.5. Confirm . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
2.4.6. Boolean . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
2.4.7. Enumeration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 17
2.4.8. Custom . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 17
2.4.9. Error texts for Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 17
2.5. Repeatable Groups and Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 18
2.5.1. Index Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 21
2.5.2. Asterisk operator. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 21
2.6. Field lists . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 23
3. Rules . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 23
3.1. Rule Group . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 23
3.2. Rule condition . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 24
3.3. Error Field . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 25
3.4. Rules and repeatability . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 26
3.4.1. Iteration. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 26
3.4.2. Semantic Index . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
3.4.3. Parallel Iteration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 28
3.4.4. Filter operator . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 30
3.5. Treatment of non-specified values. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 35
3.6. Error texts for Rules. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 37
4. Operators and language constructs . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 42
1

-- 1 of 261 --

4.1. Logical operators . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 42
4.2. The Field value operator. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
4.3. Arithmetic operations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 44
4.3.1. Overview of arithmetic operations. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 44
4.3.2. Accuracy of arithmetic operations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 45
4.3.3. Treatment of division by zero . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 45
4.3.4. Examples for using the Power Operator . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 45
4.4. String operations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 46
4.5. Comparison operations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 48
4.5.1. Comparing numbers . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 48
4.5.2. Comparing strings and enumerations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 49
4.6. Predicate language constructs. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 50
4.6.1. Predicate language constructs for individual Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 50
4.6.2. Predicate language constructs for Field lists . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 52
4.6.3. Choosing the right 'ValueList'-language construct . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 61
4.6.4. Predicate language constructs for Groups . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 63
4.7. Functional language constructs . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 66
4.7.1. Functional language constructs for single Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 66
4.7.2. Functional language constructs for Field lists and Group lists . . . . . . . . . . . . . . . . . . . . . . . . 68
4.7.3. Detecting the current repetition . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 71
4.8. Arithmetic language constructs . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 72
4.8.1. Arithmetic language constructs for single Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 72
4.8.2. Arithmetic language constructs for value lists . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 75
4.9. Date and Time language constructs . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 80
4.9.1. Date and time calculations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 81
4.9.2. Date and time constructs . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 96
4.9.3. Date fragments . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 102
4.9.4. Partially known dates . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 104
4.9.5. Date Ranges . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 106
4.10. Miscellaneous . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 110
4.10.1. PatternMatched and PatternViolated . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 110
4.10.2. Valid and Invalid. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 113
4.11. CustomCondition . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 114
5. Computation Rules. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 115
5.1. Computation tables . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 115
5.2. Preconditions. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 117
5.3. Number of decimal places . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 118
5.4. Common Precondition . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 118
5.5. Repeatable computed Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 120
5.6. Non specified Fields used in the Computation term . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 120
5.7. Calculation cycles . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 122
2

-- 2 of 261 --

5.8. Computation Rules and Parallel Iteration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 122
5.9. Computation Rules and current repetition . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 123
5.10. Computation Rules as Validation Rules . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 124
6. Language constructs in alphabetical order . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 127
7. Appendix . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 130
7.1. Validation and formal validation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 130
7.1.1. Introduction . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 130
7.1.2. Formal errors. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 130
7.1.3. Errors arising from validation rules . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 132
7.2. Full Validation and Partial Validation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 135
7.3. Mandatory fields have an asterisk in their label . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 136
7.3.1. Introduction . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 136
7.3.2. Required checkbox. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 136
7.3.3. The star method for validation rules . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 137
7.3.4. Computations. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 152
7.4. Modifying error conditions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 153
7.4.1. Make a condition always become true . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 153
7.4.2. Make a condition never become true . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 153
7.4.3. Referencing additional fields. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 153
8. Parser messages . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 154
8.1. Grammar . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 154
8.2. Error Field and Iteration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 155
8.3. Parallel Iteration. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 161
8.4. Enumeration Fields and Categories . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 169
8.5. Arithmetic . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 172
8.6. RepetitionNotUnique . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 181
8.7. SumOfProducts . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 188
8.8. Date . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 194
8.9. NumberOfValueInFields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 208
8.10. Paths . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 211
8.11. Pattern . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 214
8.12. Semantic Index . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 217
8.13. Field Types . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 226
8.14. Number and Uniqueness of Arguments . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 235
8.15. Asterisk Operator . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 238
8.16. Filter Operator and SumOfTerms. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 243
8.17. Other . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 252
8.18. Parser Messages for Computation Rules . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 261
3

-- 3 of 261 --

1. Preface
1.1. Kernel Language
A12 contains a powerful and versatile language used for the validation of user input. Rules can be
defined, based on business requirements, that make it possible to specify all conceivable types of
Field-related validation tasks. The validation language is one of the core features of A12, which
provides a method of validating data completely. This is critical in business applications to avoid
security risks and ensure data integrity. Using the language, business analysts and domain experts
can map their business constraints or requirements into Validation Rules. Rule conditions can
apply to several Fields and Groups. The language contains many predefined predicates made to
handle typical validation tasks. It supports nested comparisons and arithmetic operations as well as
special operators, such as date specifications. Furthermore, the language supports the specification
of special conditions that examine which combinations of Fields may or may not be specified. The
existing predicates and operations can be combined.
The validation language has the following key features:
• A Rule always describes the error condition or error scenario. This enables the end-user to
receive precise messages relating to the corresponding error scenario.
• With the help of the logical operators And and Or, predicates can be combined.
• There is no generic negation operator. Instead, the different predefined predicates are offered
in positive and negative forms, each containing specialized verification logic that checks the
defined Rule. Examples of this are: NoFieldFilled, AllFieldsFilled, AtLeastOneFieldFilled,
NotExactlyOneFieldFilled and FieldsNotCollectivelyFilled. This approach allows the predicates
to remain simple and be easily combined into a logical, uniform, and clear Rule condition. It
also serves to make the Rule conditions readable and more easily maintained.
• The predicate logic is expanded upon not through additional logical parts of the language, but
rather through implicit, predefined language constructs containing corresponding references.
This ensures that the defined terminology is based on the formulations of business analysts,
thus making the language more comprehensible.
• Additional logical operators allow the direct querying of hierarchies and Groups.
• Sets and filter operations are supported for hierarchies and Groups, e.g. "calculate the sum of all
capital gains from all equity funds".
• The use of iterations within hierarchical structures facilitates shortening and simplifying Rule
conditions.
• Calculations and validations are based on the same language. This allows the entire validation
language to also be used for the definition of the calculation’s preconditions. Additionally, all set
and filter operations can be utilized to formulate the calculations.
If a Rule condition is valid, an error message is displayed. There are three types of Rules:
• error Rules
• warning Rules
4

-- 4 of 261 --

• information Rules
The differentiation of Rule types assists in defining how the occurrence of a Rule is classified and
should be interpreted:
• error Rule: the input is invalid and the form cannot be saved in this state ("You entered a house
number, but the street name is missing.")
• warning Rule: some signs indicate the input is not correct, e.g. that the user mistyped part of the
input ("You entered a profit share of 70%. Are you sure this number is correct?")
• information Rule: a trigger for the display of information ("Your company’s profit exceeds
1,000,000 euros. Please consider the investment possibilities that are available.")
The main purpose of this categorization is for the design of the user interface. It does not play any
role in the validation language.
1.2. Who should read this document?
This document is intended to serve as a guide and reference for the business analysts responsible
for specifying the business Model and plausibility Rules of the Document Model. This document is
not intended to be a manual for the input editor, but rather a tool defining how the Rules of a
Document Model should be formulated.
1.3. Model examples
Three example Models are used in this document:
• Model Account
• Model Hotel Royal
• Model Employees
2. Models
2.1. Model structure
A Model consists of Groups, Fields, Rules (including Computation Rules). A Group can contain
Fields, Rules, and other Groups. Fields and Rules must always exist in a Group. Allowing Groups to
exist within another Group gives Models a tree structure.
Models are used to describe data structures and Validation Rules, such as the structure of a form.
An example of a form:
5

-- 5 of 261 --

Figure 1. Account form
The Model which describes the form:
6

-- 6 of 261 --

Figure 2. Model 'Account'
The Model Account contains the following Groups: Account, Addresses, Order, and Products.
A Group in a Model can consist of additional Groups as well as Fields and Rules. For example, the
Group Account contains:
• Field AlternateDeliveryAddress (Fields are displayed in blue)
7

-- 7 of 261 --

• Rule AltDelAddress (Rules are displayed in green and italics)
• Groups Addresses and Order
Rules help to define constraints for the Field values in a Model. The core element of a Rule is its
Rule condition, which must always describe the error condition.
Examples
The Rule condition
FieldFilled(FirstName) And FieldNotFilled(LastName)
describes the following error: FirstName was specified, but LastName was not specified.
This Rule condition ensures that LastName is specified if FirstName has been specified.
The Rule condition
FieldFilled(AlternativeDeliveryAddress) And GroupNotFilled(Addresses for "AltDel").
describes the following error: AlternativeDeliveryAddress is specified, but no alternative delivery
address is given.
The Rule condition ensures an alternative delivery address is specified if the Field
AlternativeDeliveryAddress — which can, for example, also be represented by a checkbox — has
been specified.
Groups help to define the structure of a Model. Additionally, Groups can be referenced in the
validation language.
The partial condition
GroupNotFilled(Products)
is equivalent in meaning to the partial condition
NoFieldFilled(ProductNumber,ProductName,UnitPrice, Quantity, TotalPrice)
The following terminology is used to denote the Group structure of a Field:
• The Field Quantity is directly contained in Group Products.
• The Field Quantity is indirectly contained in Group Order.
• The Field Quantity is indirectly contained in Group Account.
The name of all Fields, Rules, and Groups within each Group of a Model must be unique. If two
8

-- 8 of 261 --

Fields are included in the same Group, then their name must be different.
2.2. Paths
2.2.1. Absolute paths
The absolute path of Field ProductNumber is
/Account/Order/Products/ProductNumber
The absolute path of Rule AltDelAddress is
/Account/AltDelAddress
The absolute path of Group Products is
/Account/Order/Products
The absolute path is utilized to explicitly identify a Field. This is necessary in the event that a Field
name is not unique.
• Group Account lies above Group Products.
• Group Products lies below Group Order.
2.2.2. Relative paths
Relative paths are also used to identify a Field in a Model. The relative path always starts from the
Rule Group (the Group in which the Rule is located).
If the Field lies directly in the Rule Group, only the Field name is used.
Starting with Rule Group Products, Field ProductNumber has the following relative path:
ProductNumber
Groups lying above the selected Rule Group can also be referenced using upward references. To
reference the Group above the Rule Group, ".." is used as notation, with each ".." indicating that you
have gone up one Group in the path from the Rule Group. Similarly, "../.." would be used to
reference the Group lying two Groups above the Rule Group and so forth. Additionally, the name of
the highest Group (or the "turning Group") can optionally be specified after the last "..". Explicitly
9

-- 9 of 261 --

naming the turning Group ensures that the Group being referenced is easily recognizable. If the
turning Group is the root Group of a Model that will be used as a complex type or as an include, the
name of the turning Group should not be explicitly specified in the path, in order to keep the path
independent of the name of the include.
Starting with the Rule Group Products, the relative path for Field OrderDate would be:
..Order/OrderDate
The turning Group Order is explicitly specified.
In the following variant of the Model Hotel Royal,
the Field Amount can be referenced from Rule Group Poker using the relative path
../../..Account/O/Hotel/Amount
where Account is the specified turning Group.
Alternatively, without turning Group:
../../../O/Hotel/Amount
The difference between absolute and relative paths is easy to spot: absolute paths begin with "/"
whereas relative paths do not.
The Rule Group itself can be addressed by
10

-- 10 of 261 --

RuleGroup
If, for example, a Rule is directly contained in the Group Addresses of the Model 'Account', the
condition
AllFieldsFilled(/Account/Addresses)
is equivalent to the condition
AllFieldsFilled(RuleGroup)
2.3. Constants and data types
A constant is an immutable value of a certain (data) type:
• Number (e.g. 17 or 0.177)
Decimal places are separated by a point. A negative sign is represented by a minus sign.
Example:
[Limit] == -10.9
• String (e.g. "Grain" or "Munich")
Example:
[Name] == "Huber Isolde"
• Date (e.g. "19.04.2000")
A date constant is a string constant that conforms to the format
DD.MM.YYYY
i.e. first the day, then the month, and then the year, with a period used to separate each value.
Example:
[Begin] == "19.04.2000"
• Date without year (e.g. "19.04.")
11

-- 11 of 261 --

A date constant without year is a string constant that conforms to the format
DD.MM.
i.e. first the day, then a period, then the month, then a period.
• Time (e.g. "23:49:59")
A time constant is a string constant that conforms to the format
HH:mm:ss
i.e. first the hour, then the minute, and then the second, with a colon as separator.
• Date Time (e.g. "19.04.2000T23:49:59")
A date time constant is a string constant that conforms to the format
DD.MM.YYYYTHH:mm:ss
i.e. first a date constant, then the separator T, then a time constant.
• Boolean (True, False)
Defining data types helps to specify
• what kind of input is expected for a Field (see Field types),
• what type of argument is expected for a language construct, and
• what kind of value a functional language construct returns.
2.4. Field types
The second example presented is a Model based on a hotel with an attached casino. For legal
reasons, certain data must be collected from both the hotel guests and the casino visitors. Each
guest has a unique customer_id. The customer id is Modeled by an Index Field of type number.
The following data must be collected for each hotel stay:
• the customer id
• the check in date
• the checkout date
• the room type
• the invoice amount
The following data must be collected for each visit to the casino:
12

-- 12 of 261 --

• the customer id
• the date of the visit
• the invoice amount
• the total win-loss for up to 5 rounds of poker
According to law, the win-loss in poker must be registered and a maximum of 5 rounds are allowed
per visit.
Figure 3. Model 'Hotel Royal'
The # character to the right of the Field name is indicative of an Index Field.
The Field type determines which kind of data is permitted as a Field value and how this data, i.e. in
a comparison, should be interpreted. The Field type can be further specified through the use of
additional properties.
13

-- 13 of 261 --

The following Field types are supported:
• String
• Number
• Date, Time, DateTime
• DateFragment
• DateRange
• Confirm
• Boolean
• Enumeration
• Custom
• TypeDefinition
Hence, the permitted Field values of the Model Hotel Royal can be set as follows:
Table 1. Example of Field attributes
Field Type Further attributes
CustomerId Number Whole number between 1 and
1000.
Leading zeros are allowed.
CheckIn
CheckOut
Date Date in the format DD.MM.YYYY
Amount Number Null value is allowed.
Minus sign is not allowed.
Category Enumeration Permitted values: DZ, EZ,
JuniorSuite, SuiteRoyal.
Round Number Whole number between 0 and
100.
Win_Loss Number Number with two decimal
places.
Null value is allowed.
Minus sign is allowed.
2.4.1. String
The Field type string can be further specified using the attributes listed in the following table.
Table 2. String attributes
14

-- 14 of 261 --

Attribute Example Meaning
Minimum length 2 Specification of the minimum
length. The minimum length
cannot be smaller than 1.
Maximum length 10 Specification of the maximum
length.
Pattern [A-Z]{2}[0-9A-Z]{10} Specification of a regular
expression - see also overview
regular expressions
Error message Invalid ZIP Code. Specification of an error
message - only allowed if a
pattern is specified.
Line Breaks Permitted True Specifies whether line breaks
are allowed.
Hint List [{"locale": "de_DE", "values":
["eins","zwei","drei"]},{"locale":
"en_US", "values":
["one","two","three"]}]
Specifies multiple suggestions
(hint list) for data input into a
string Field, so that a user can
quickly choose one of these
predefined values but still can
add a custom value.
NOTE
Length of strings which contain combined characters. If the chosen character set
allows combined characters like C̨̆ , which is defined by the Unicode code point
sequence (0x0043, 0x0328, 0x0306), then the length of this combined character is
given by the length of the defining sequence. Thus, C̨̆ has length 3. For example, if
a Field of type string has Maximum length 6, the value C̨̆elin is not valid, since this
counts as a string of length 7.
2.4.2. Number
The Field type number can be further specified using the attributes listed in the following table.
Table 3. Number attributes
Attributes Example Meaning
Min. Decimal Places 5 Specification of the minimal
number of decimal places.
Max. Decimal Places 2 Specification of the maximal
number of decimal places.
Minimal value 5 Indicates the minimal value
allowed.
Maximal value 100 Indicates the maximal value
allowed.
15

-- 15 of 261 --

Attributes Example Meaning
Zero allowed True Indicates if zero is allowed.
Leading Zeros Allowed True Indicates whether leading zeros
are allowed - e.g. 001.
2.4.3. Date, Time, DateTime
The Field type date can indicate a date, time, or a date with a time. Partial date specifications are
supported by this Field type.
NOTE Dates prior to October 16, 1583 are invalid.
NOTE
Date fragment
A date can be specified that does not contain all information corresponding to the
day, month or year. This is known as a DateFragment. In this case, restrictions are
applied to the use of date language constructs as well as comparisons. These
restrictions are further explained in Section 4.9.3.
2.4.4. DateRange
The Field type DateRange is used to describe an interval of time through the specification of two
dates (without time).
2.4.5. Confirm
A Field of type Confirm can have the value True. Thus, for such a Field there are 2 options:
• The Field is not filled.
• The Field has the value True.
2.4.6. Boolean
A Field of type Boolean can have the values True and False. Thus, for such a Field there are 3
options:
• The Field is not filled.
• The Field has the value True.
• The Field has the value False.
TIP
This means that if a Boolean is required it can be True or False, so either is a valid
value, but if a Confirm type is required the only valid input is True. Use a Boolean to
allow your end user to either accept or decline your cookie policy. If there is no
explicit decision, the user will see an error. Choose a Confirm if you want your user to
accept the privacy policy.
16

-- 16 of 261 --

2.4.7. Enumeration
For a Field of type enumeration, only a specified list of string values is allowed. Additional
categories can be defined for an enumeration. For each enumeration Field, the defined categories
must have unique names. A value must be specified for each enumeration value and category. The
value of a category contained in a Field can be queried - see also further details.
This Field type can be further specified using the attributes listed in the following table.
Table 4. Enumeration attributes
Attribute Example Meaning
Error text No valid country was selected. Optional specification of an
error text.
S-value Germany Specification of a specially
selected enumeration value.
NOTE
The S-value can be used for Index Fields. An Index Field normally has to be filled if
the Group which contains the Index Field is filled. However, the following exception
applies to the Index Field if the S-value is specified: the S-value is used if no value is
specified and the Index Field in the current data set has only 1 iteration. Hence, a
Model could be created in the above example (if only one land is being specified),
without having to explicitly specify Germany as the value.
2.4.8. Custom
The Field type custom is used for all data types that require a specialized validation algorithm, such
as an IBAN, a BIC, or other specific registration numbers. The Rule condition for this Field type is
treated the same as for strings.
2.4.9. Error texts for Fields
In some cases, error texts can also be defined for Fields, as a part of the Field definition.
• For string Fields with pattern, an error message must be specified. The error is shown if the
pattern is violated.
• For enumeration Fields it is optional to specify an error text. The error is shown if the value of
the Field is not a valid enumeration value. If no error message is specified, a default text is used.
The following error text parameters are available:
Table 5. Error text parameters for Fields
Parameter Description Example
Field References the name of the
Field.
The value of $Field$ must be a
5-digit number.
Field.value References the value of the
Field.
$Field.value$ is not a 5-digit
number.
17

-- 17 of 261 --

NOTE
The error text parameters for Fields are literally Field and Field.value,
independent of the name of the Field. For example, if the name of the Field is
Postcode, you have to use Field.value and not Postcode.value (as in the case of Rule
error texts).
2.5. Repeatable Groups and Fields
Every Group has a defined repeatability that must be a positive integer.
Let’s consider another example Model.
Figure 4. Model 'Employees'
A Group’s repeatability is indicated in square brackets next to the Group name.
The Group Countries has a repeatability of 3.
A Group is repeatable if its repeatability is greater than 1.
18

-- 18 of 261 --

The Group Internal is not repeatable.
The Group Subsidiaries is repeatable.
Since the Group Countries has a repeatability of 3, there are 3 possible instances for the Field
Country to be defined. This means that up to 3 countries can be specified.
Field instance Value
/Internal[1]/Countries[1]/Country Germany
/Internal[1]/Countries[2]/Country Italy
/Internal[1]/Countries[3]/Country Sweden
More generally speaking, there are 3 possible instances for the Group Country itself to be defined.
If a Field’s path contains multiple repeatable Groups, then the number of possible instances is
multiplied. For example, the Field Subsidiary may have up to 6 instances.
Table 6. Values of Field instances
Field instance Value
/Internal[1]/Countries[1]/Subsidiaries[1]/Subsidi
ary
Berlin
/Internal[1]/Countries[1]/Subsidiaries[2]/Subsidi
ary
Hamburg
/Internal[1]/Countries[2]/Subsidiaries[1]/Subsidi
ary
Rome
/Internal[1]/Countries[3]/Subsidiaries[1]/Subsidi
ary
Stockholm
/Internal[1]/Countries[3]/Subsidiaries[2]/Subsidi
ary
Malmö
A document consists of a set of Field values and their corresponding Field instances. Datasets are
often notated in tabular form. There are 5 specified instances of the Group Subsidiary in the
document in Table 6. In other words, the Group Subsidiaries (or the Field Subsidiary) is specified 5
times.
Since we can specify
• up to 3 countries,
• up to 2 subsidiaries per country and
• up to 5 employees per subsidiary,
the Field Name has 30 (30 = 30 * 2 * 5) possible instances.
A repeatable Group in the path of a Field is also called repetition layer of that Field.
19

-- 19 of 261 --

The Field LastName has 3 repetition layers.
A Field is repeatable if it has at least one repetition layer (in other words: the absolute path of the
Field contains at least one repeatable Group).
NOTE When specifying the values of a repeatable Field, each value’s corresponding Field
instance must be given.
For simple documents, such as
Field instance Value
/Account[1]/Order[1]/Products[1]/ProductNumber 10
/Account[1]/Order[1]/Products[2]/ProductNumber 20
/Account[1]/Order[1]/Products[3]/ProductNumber 5
we use also notations like
Summary:
• Each Group has repeatability
• Not every Group is repeatable
• A Group’s repeatability is independent of its path. This attribute pertains solely to the
specified Group.
• On the other hand, a Field’s repeatability is an attribute of its path and not the Field itself.
• The number of possible instances of a Field or Group is considered a property of the Model.
• The number of specified instances of a Field or Group is considered a property of the
document.
Note the difference between
/Account[1]/Order[1]/Products[2]/ProductNumber
and
/Account/Order/Products/ProductNumber
20

-- 20 of 261 --

The former denotes an instance of a Field, whereas the latter denotes the path of a Field.
2.5.1. Index Fields
For every Group, one Field can be specified as the Index Field. With the help of Index Fields,
specific instances of repeatable Groups can be addressed using the semantic index, or repetitions of
different repeatable Groups can be connected using the Parallel Iteration, respectively.
Index Fields are denoted with # in the graphical representation of a Model.
The following is automatically validated for an Index Field (corresponding Rules are generated
automatically):
• The Index Field must be specified if the Group of an Index Field has been specified
(GroupFilled(RuleGroup) and FieldNotFilled(indexField))
• An Index Field’s specified values must be unique within each iteration
(RepetitionNotUnique(indexField))
2.5.2. Asterisk operator
An asterisk is used to combine all instances of a repeatable Field or repeatable Group into a list.
The following notation,
/Account/Hotel*/CheckIn
represents the list of all 10 possible instances of the Field /Account/Hotel/CheckIn.
In the case of several repetition layers, multiple repeatable Groups in a path may be marked with
an asterisk.
The following notation,
/Account/Casino*/Poker*/Win_Loss
represents the list of all 50 possible instances of the Field /Account/Casino/Poker/Win_Loss.
This Field list can therefore contain up to 50 values:
21

-- 21 of 261 --

Figure 5. Field list /Account/Casino*/Poker*/Win_Loss
Figure 5 provides another chance to visualize the difference between possible instances and
specified instances:
• The Field has 50 (10 for the <em>Casino</em> &times; 5 for <em>Poker</em>) possible
instances. This is an attribute of the Model.
• The Field has 3 specified instances. This is an attribute of the document.
If there are multiple repetition layers, it is acceptable for only some of the repeatable Groups to
have an asterisk.
The following notation,
/Account/Casino/Poker*/Win_Loss
represents (for each fixed iteration of Casino) the list of all 5 instances of the Field
/Account/Casino/Poker/Win_Loss.
However, the following also applies: if a repeatable Group in a path is notated with an asterisk, all
additional repeatable Groups which are below this repeatable Group must also have an asterisk.
Thus, the variation
/Account/Casino*/Poker/Win_Loss
is not allowed.
22

-- 22 of 261 --

2.6. Field lists
A Field list is a list of Fields or Field instances.
Basic elements of a Field list Example
Field path /Account/Addresses/FirstName
Field path with semantic index /Account/Hotel/CheckIn For "1"
Group path /Account/Order/Products
Group path with semantic index /Account/Hotel For "1"
Field path with * (see also asterisk operator) /Account/Order/Products*/ProductName
Group path with * /Account/Order/Products*
Field Path with * and Filter Operator /Account/Order/Products*/ProductName Having
[/Account/Order/Products/UnitPrice] > 10
combination of types listed above /Account/Addresses/FirstName,
/Account/Addresses/LastName,
/Account/Addresses/Company,
/Account/Order/Products*
A Group defined as a Field list will be replaced by all Fields contained within that Group. This
includes the Fields of all direct and indirect subGroups.
CAUTION
When a Field list is used as an argument in a language construct, then a Field is
not allowed to occur more than once per Field list — the same Field being
referenced (directly or indirectly via a Group) in different ways is also not
permitted.
Some language constructs have additional restrictions that apply. These are documented and can be
found in the description of the respective language constructs.
3. Rules
Rules consist of a Rule condition and an error text that should be output when the Rule condition
holds (which describes an error situation). The following items list the important components of a
Rule:
• Rule Group
• Rule condition
• error Field
• error text
3.1. Rule Group
A Rule Group is the Group in which a Rule is contained. The Rule Group is essential for the
23

-- 23 of 261 --

formulation of Rules, since it is the starting point of a relative path.
NOTE
It is beneficial to choose a Rule Group which contains all or most of the Fields and
Groups referenced in the Rule condition: relative path notations are shorter and
remain valid in case the entire Rule Group is moved within the Model.
3.2. Rule condition
The Rule condition of a Rule can reference Fields, Field lists, and Group lists as arguments.
• A Field is said to be directly referenced if it is found in the Rule condition.
• A Field is said to be indirectly referenced if it lies either directly or indirectly in a Group that is
given in the Rule condition.
A Rule condition can be the combination of one or more partial conditions. If multiple partial
conditions exist, these are connected with logical operators And or Or. The following types of
partial conditions exist:
Table 7. Types of partial conditions
Type Example
language construct AllFieldsFilled(FirstName,LastName,Company)
comparison NumberOfFilledFields(Account*/Hotel) >= 5
verification Valid(Date(DayField, MonthField, "2019"))
The correctness of a Rule requires in particular the syntactical correctness of its Rule condition. The
language constructs must be combined correctly and be filled with correct arguments. This is
further described in operators and language constructs. Additionally, certain structural conditions
must be met regarding repeatable Groups.
You can also add comments before and after each partial condition or language construct.
Comments start with ;; and reach to the end of the corresponding line.
A Group is repeatably referenced in the Rule condition if:
• the Group is referenced in the Rule condition or occurs in the path of a referenced Field or a
referenced Group,
• the Group is repeatable,
• the Group is not referenced with an asterisk and,
• the Group is not referenced together with the semantic index.
In the Rule condition
AllFieldsFilled(Casino/Poker*/Round) ;; Casino is repeatably referenced
the Group Casino is repeatably referenced, while the Group Poker is not.
24

-- 24 of 261 --

A Field that is referenced directly or indirectly via a Group is said to be repeatably referenced if
its absolute path contains a repeatably referenced Group. A Rule iterates if the error Field is
repeatably referenced — see also iteration.
A path specified in a Rule condition can either be an absolute path or a relative path.
NOTE
The validation language contains keywords, such as And, FieldFilled, Today, and
Date, that may also be used as the name of a Field or Group. In this case, such names
must be specified in the Rule condition using single quotes, e.g.
FieldFilled(Casino/'Date') And [Casino/'Date'] == [Hotel/Checkin]
The following table provides an overview over different kinds and usages of brackets in Rule
conditions.
Table 8. Kinds of brackets
Bracket Usage Example
( ) enclose arguments of a language
construct
FieldFilled(Name)
( ) structure for partial Rule conditions [CustomerID]==7 And (
FieldNotFilled(Amount) Or
FieldFilled(Category) )
[ ] Field value operator [ProductNumber] == 10
{ } structure arithmetical expressions { 2 + [TotalPrice] } * 4
3.3. Error Field
The error Field of a Rule is the Field on which the error is usually displayed on the A12 web
interface. Every Rule is required to have a specified error Field. When choosing an error Field,
certain restrictions apply. These are explained through the following example using the Model
Employees. Consider the following Rule containing
• Rule Group = Countries
• Error Field = LastName
and condition
[/ApprovalStatus/ProcessCompleted] == True and
[Country] == "USA" and GroupFilled(Subsidiaries/Employee) and
FieldNotFilled(Subsidiaries/Employee/LastName)
1) The error Field is referenced in the Rule condition
In this example, the error Field is directly referenced in
FieldNotFilled(Subsidiaries/Employee/LastName)
25

-- 25 of 261 --

The indirect referencing of the Field LastName in
GroupFilled(Subsidiaries/Employee)
would have been sufficient to satisfy condition 1).
2) The error Field is directly or indirectly included in every repeatably referenced Group
In this example, the repeatably referenced Groups are Subsidiaries and Employee. These Groups
either directly or indirectly contain the error Field. It would not be permitted to choose the Field
Country as the error Field.
Condition 2) does not need to be true for Parallel Iteration.
3) If the error Field is repeatable, it must be directly or indirectly included in the Rule Group
For example, choosing the Group /ApprovalStatus as the Rule Group is not allowed.
3.4. Rules and repeatability
3.4.1. Iteration
A Rule iterates if the error Field is repeatably referenced. In principle, iteration takes place for
every repeatably referenced Group of the absolute path of the error Field. However, the Rule only
iterates over the repetitions which are specified in the document, but at least one validation takes
place.
A Rule with Rule Group Casino, error Field Round, and Rule condition
FieldFilled(Poker/Round) And [Poker/Win_Loss] >= 0
is therefore evaluated up to 50 times.
Consider the following document:
In this case, the Rule specified above is evaluated four times:
26

-- 26 of 261 --

• for the first iteration of Casino and the first corresponding iteration of Poker
• for the first iteration of Casino and the second corresponding iteration of Poker
• for the second iteration of Casino and the first corresponding iteration of Poker
• for the second iteration of Casino and the second corresponding iteration of Poker
NOTE The use of negative conditions, i.e. conditions that become true when no Field is
specified, is forbidden in combination with repeatable Groups.
3.4.2. Semantic Index
The semantic index is used to reference a specific repetition of a Group. This requires the desired
Group to have an Index Field. The value of the semantic index can be specified as either a constant
or a Field. If an additional Field is specified, the Field’s value determines the value of the Index
Field.
In the Model Account,
Field Type — an enumeration Field with enumeration values BillingAddress and
DeliveryAddress — is an Index Field. Hence, you can use it to directly access an instance of the
Group Addresses.
The condition
[AlternativeDeliveryAddress] == True And GroupNotFilled(Addresses For "Delivery Address")
is satisfied if the Field AlternateDeliveryAddress (which is of type confirm) is specified, but no
instance of Addresses with Type = DeliveryAddress is specified.
27

-- 27 of 261 --

The condition
GroupFilled(Addresses For "Delivery Address") And
GroupNotFilled(Addresses For "Billing Address")
is satisfied if the delivery address is specified and the billing address is not.
Let’s assume the Model Account contains an additional non-repeatable Field PreferredAddress,
which is also an enumeration Field with enumeration values BillingAddress and DeliveryAddress.
The partial condition
[Addresses/Country For PreferredAddress] == "Switzerland"
is satisfied if
• the Field PreferredAddress is specified,
• an instance of the Group Addresses is specified with a defined Type that coincides with that of
the Field PreferredAddress and
• the value of Field Country is specified as Switzerland for this instance.
However, the following restrictions apply:
• The use of the semantic index is not supported for more than one repetition layers and several
Index Fields.
• The use of the semantic index is not supported for NumberOfFilledFields and
RepetitionNotUnique.
• The use of the semantic index is not supported when an encompassing Group has an asterisk.
3.4.3. Parallel Iteration
The term Parallel Iteration refers to situations where the Rule condition contains two repeatably
referenced Groups G1 and G2 such that G1 lies neither below nor above G2.
A Parallel Iteration is only allowed if an Index Field is defined for every Group over which a
Parallel Iteration occurs. The iterations are thus defined based on the value of the specified Index
Fields.
The following verifications are performed:
• An Index Field must be defined for every Group over which a Parallel Iteration occurs. The
Index Fields must have the same name and type.
• The path of the error Field as well as the Groups that are iterated over in parallel are not
allowed to have more than one Index Field.
• Parallel Iterations are not allowed to occur over Rules that use the language construct
RepetitionNotUnique.
28

-- 28 of 261 --

• Negative conditions are not allowed to be used in a Parallel Iteration, except when used in
combination with a positive condition. This ensures that the use of a negative condition does
not require a value to be defined for every iteration.
Furthermore, each Parallel Iteration occurs over a set of Index Field values. Throughout a Parallel
Iteration, if no iteration for a Group contains a specified Index Field value, then this Group (along
with all of its subGroups and Fields) would be considered "not specified" during the Rule
evaluation.
Let’s consider a Rule with the error Field Hotel/Amount and Rule condition
FieldsNotCollectivelyFilled(Hotel/Amount,Casino/Amount)
and the following document:
The Rule iterates over the following Index Field values: 77, 78, 79, 80.
In this scenario, two of the four cases (case 77 and 79) would return an error:
• The first iteration of Hotel:
Hotel/Amount for CustomerId = 77 is specified
Casino/Amount for CustomerId = 77 does not exist, which counts as not specified
• The third iteration of Hotel:
Hotel/Amount for CustomerId = 79 is not specified
Casino/Amount for CustomerId = 79 is specified
29

-- 29 of 261 --

3.4.4. Filter operator
The filter operator can be specified using the following format:
FieldList Having FilterCondition
The filter operator is used to filter out values of a Field list based on a defined FilterCondition.
The condition
Sum(Products*/Quantity Having [Products/ProductName]=="N33") > 100
states that the order contains more than 100 exemplars of the product N33. The validation of
document
returns the following value list for Field list Products*/Quantity:
77, 76, 88, 20, 14, 8, 99.
The filter condition [Products/ProductName]=="N33" filters out the iterations of Products where
[Products/ProductName]=="N33" and therefore returns the following value list: 76, 20, 8. Since the
sum of these values is > 100, the Rule condition holds.
A filtered Field list can be used in the same manner as an unfiltered Field list. Hence, all language
constructs that have a Field list as one of their parameters can also use a filtered Field list.
However, the following restrictions do apply:
The Field list
• must consist of a Field path with an asterisk
• may not contain a semantic index
The filter condition
• is a valid condition
• specifies an iteration for at least one repetition layer which the Field list has denoted with an
asterisk
• does not use
◦ Parallel Iterations
30

-- 30 of 261 --

◦ a semantic index
◦ another filter operator
◦ SumOfTerms
◦ RepetitionNotUnique
◦ CustomCondition
The filter operator can also be used on multiple repetition layers.
Example 1
A Rule with Rule condition
AtLeastOneFieldValueIncludedInValueList(Countries*/Subsidiaries*/Employee*/LastName
Having [Countries/Subsidiaries/Employee/BeginEmployment] < "01.01.2020" IN
"Huber","Mayer")
does not iterate because all of the repeatable Groups are notated with an asterisk.
Of the 30 potential employees, only those that began their employment before 01.01.2020 are
filtered out of the list. The condition is valid if at least one of the remaining employees has the
name Huber or Mayer.
Example 2
A Rule with Rule condition
NumberOfFilledFields(Countries/Subsidiaries*/Employee*/LastName
Having [Countries/Subsidiaries/Employee/BeginEmployment] < "01.01.2020") == 2
iterates over all specified instances of the Group Countries because the repeatable Group Countries
does not have an asterisk, but all other repeatable Groups do.
For each country, the condition is valid if there are 2 (out of 10) employees that began their
employment before 01.01.2020, i.e. exactly 2 of the 10 employees are filtered out of the list.
Example 3
A Rule with Rule condition
NumberOfFilledFields(Countries/Subsidiaries/Employee*/LastName
Having [Countries/Subsidiaries/Employee/BeginEmployment] < "01.01.2020") == 2
iterates over all specified instances of the Groups Countries and Subsidiaries, because the
repeatable Groups Countries and Subsidiaries do not have an asterisk.
For each country and subsidiary, the condition is valid if there are 2 (out of 5) employees that began
their employment before 01.01.2020.
31

-- 31 of 261 --

Iteration in filter condition
There is also the possibility to point to the current repetition in the filter condition. This can be
achieved by writing the symbol $ in front of a Field or Group reference. Recall that the general
format for the filter operator is
FieldList Having FilterCondition
where FieldList is a path of a Field with at least one * and FilterCondition is a partial rule condition.
Consider the following Model.
The Group PublicHolidays contains the public holidays of the year.
It is not allowed to define a field list like
Orders*/PlannedDeliveryDate
Having [Orders/PlannedDeliveryDate]==[PublicHolidays/HolidayDate]
because the repeatable Field HolidayDate is not in the group Orders. In this situation, we can
replace the Field reference
PublicHolidays/HolidayDate
with
$PublicHolidays/HolidayDate.
Then, the resulting field list
Orders*/PlannedDeliveryDate
Having [Orders/PlannedDeliveryDate]==[$PublicHolidays/HolidayDate]
32

-- 32 of 261 --

can be used. In this context, the symbol $ is called the <b>$-operator</b>.
Syntax of the $-operator: The $-operator may be used once or several times in a filter condition.
The $-operator must be placed in front of a Field reference ot Group reference. If this reference is
given by a path, the $ must be placed in front of the path.
Path notation: When using the $-operator, there are no special restrictions how to write the path.
If a path notation is correct without the $, it is also correct with the $. Depending on the model,
varieties like
• $/A/PublicHolidays/HolidayDate
• $PublicHolidays/HolidayDate
• $HolidayDate
may be possible.
The semantic of the $-operator is explained using the following examples.
Example 4
We want to create a Rule of level hint which iterates over PublicHolidays and returns a hint (per
holiday) if for this day more than 10 deliveries are planned. An error message would look like:
For Good Friday, more than 10 deliveries are planned. Please check the capacity.
This requirement can be met with error Field PublicHolidays/HolidayDate, Rule condition
GroupFilled(PublicHolidays) And
NumberOfFilledFields(Orders*/PlannedDeliveryDate
Having [Orders/PlannedDeliveryDate]==[$PublicHolidays/HolidayDate]) > 10
and error text
For $PublicHolidays/Holiday.value$, more than 10 deliveries are planned. Please check the
capacity.
The rule iterates over the instances of the group PublicHolidays. The second part of the Rule
condition counts the number of deliveries using the NumberOfFilledFields operator. A list of all the
instances of PlannedDeliveryDate is created using the * operator which is then filtered by the filter
operator, Having. In Examples 1-3, the filter operator was used to compare an instance with a
constant (e.g. "01.01.2020"). In this example, we use the operator $ to iterate through the instances
of PublicHolidays/HolidayDate and test the Rule against each instance. Here, the use of the $-
operator indicates that the value of HolidayDate of the current repetition is used. If the Rule
condition is triggered, the error message is then shown next to the respective repetition, i.e. the
instance of PublicHolidays/HolidayDate on which there are more than 10 deliveries planned.
33

-- 33 of 261 --

The Rule condition GroupFilled(PublicHolidays) is semantically redundant here, as adding or
omitting it does not change the semantic of the Rule condition as a whole. However, Fields used in
the error text need to be referenced in the Rule condition. As a result, this condition is introduced
to make sure that the parameter $PublicHolidays/Holiday.value$ may be used in the error text.
Example 5
Combining the $-Operator with CurrentRepetition makes it possible to refer to 'all predecessors' or
'all successors' of an iteration.
The Field list
PublicHolidays*/HolidayDate Having
CurrentRepetition(PublicHolidays) < CurrentRepetition($PublicHolidays)
consists of all instances of HolidayDate which are predecessors of the current repetition. This makes
it possible to express that the public holidays are not specified in chronological order:
MaxValue(PublicHolidays*/HolidayDate Having
CurrentRepetition(PublicHolidays) < CurrentRepetition($PublicHolidays))
>= [PublicHolidays/HolidayDate]
The error Field of this rule is HolidayDate. The Rule condition finds the maximal value in the
filtered list (i.e. of the previous repetitions) and compares it to the HolidayDate of the current
repetition (therefore, the $-operator is used: to point to the current repetition) of PublicHolidays. As
the Rule iterates through the instances of PublicHolidays, the index of the current iteration is
returned using the operation
CurrentRepetition($PublicHolidays)
For the third iteration of PublicHolidays, the Field list
34

-- 34 of 261 --

PublicHolidays*/HolidayDate Having
CurrentRepetition(PublicHolidays) < CurrentRepetition($PublicHolidays)
returns the value list
01.01.2022, 25.12.2022
The maximal (latest) value thereof is 25.12.2022. This date value is later than 03.10.2022. Therefore,
for the third iteration of PublicHolidays, the condition
MaxValue(PublicHolidays*/HolidayDate Having
CurrentRepetition(PublicHolidays) < CurrentRepetition($PublicHolidays))
>= [PublicHolidays/HolidayDate]
is valid: the holidays are not given in chronological order.
The $-Operator can be used for both Groups and Fields. The following must hold:
• For the * operator and iteration the usual restrictions (like the respective entity would be
referenced outside the filter condition) apply.
• $ may only be used inside a filter condition.
• Parallel Iteration is not permitted for entities marked with $.
• The semantic index cannot be used in connection with $.
• The filter condition specifies filter iteration for at least one level of the filtered area. Thus, a
Field list like
PublicHolidays*/HolidayDate Having [$PublicHolidays/HolidayDate] > "01.07.2021"
is not valid since HolidayDate is not referenced with filter iteration. In other words: it is not
possible that every entity in the filter condition is marked with the $ symbol.
• The Field or Group that is used with the $-Operator can be replaced with a constant. For
example, in Example 4, the condition [Orders/PlannedDeliveryDate]==[
$PublicHolidays/HolidayDate] could be changed to [Orders/PlannedDeliveryDate]=="29.08.2022".
Equally, the condition in Example 5, CurrentRepetition(PublicHolidays) < CurrentRepetition(
$PublicHolidays), could be change to CurrentRepetition(PublicHolidays) < 3. In both examples,
the Rule would still be valid.
3.5. Treatment of non-specified values
Rules that are missing a Field value needed for a comparison or calculation are handled differently
based on the unspecified Field’s type:
• For Fields of type string, boolean, custom, date, and enumeration: The comparison condition
following the Field is not evaluated if the Field is not specified, i.e. the condition does not result
in an error. To make the Rule condition clear to untrained readers, it can be useful to emphasize
35

-- 35 of 261 --

this by adding a precondition: FieldFilled(SubmissionDate) And [SubmissionDate] >
"01.01.2022"
Table 9. Evaluation of [SubmissionDate] > "01.01.2022"
Value Condition satisfied?
Field SubmissionDate with value "01.03.2022" Condition satisfied, i.e. the error is reported
Field SubmissionDate with value "01.01.2022" Condition is not satisfied, i.e. the error is not
reported
Field SubmissionDate not specified No error is reported
• For Fields of type number: A default value of 0 is used if no value is specified. This value is then
used in further calculations. For example, if the sum of all specified and non-specified Field
values is calculated, then 0 would always be added to the total sum for each non-specified Field
(i.e. the total sum would not change). However, the default value 0 does not apply for minimum
and maximum value calculations. In this case, non-specified Fields are ignored.
Hence, if a Field value comparison [Amount] < 100 is defined, then the Rule should additionally
examine if the Field Amount was specified. Otherwise, the Rule will also report an error if the
Field Amount was not specified.
Table 10. Evaluation of [Amount] < 100
Value Condition satisfied?
Field Amount with value 5 Condition satisfied, i.e. the error is reported
Field Amount with value 100 Condition not satisfied, i.e. the error is not
reported
Field Amount not specified Condition satisfied, i.e. the error is reported
To make the Rule condition clear to untrained readers, it is good practice to formulate the condition
as explicitly as possible, even if this seems extensive on an individual basis. For example, if an error
should be reported when the amount is not specified or is smaller than 100, the Rule condition
[Amount] < 100 is sufficient, but formulating the condition as FieldNotFilled(Amount) Or [Amount] <
100 is much clearer.
• For Fields of type confirm: Non-specified Fields in comparison conditions are treated as if the
logical value False were specified.
NOTE
Due to optimization reasons, Validation Rules which consist of comparisons, like
[Amount] < 100, are sometimes not executed if all of the referenced Fields are in the
same root Group and this root Group is not filled. This can be avoided by adding a
partial condition that evaluates whether the Fields used in the comparison are
specified or not.
NOTE
If a Rule contains a comparison, adding a partial condition that evaluates whether
the Fields used in the comparison are specified or not is good practice, especially if
it is not obvious from the existing Rule condition what the consequence of a missing
Field would be. Additionally, this helps to ensure that the Rule condition is clearly
36

-- 36 of 261 --

formulated. For example, it is clear, without adding the operator, that the
comparison [SubmissionDate] == "01.01.2022" would be false if the Field
SubmissionDate was not specified. Hence, in this case, the precondition
FieldFilled(SubmissionDate) And … can be omitted without compromising the
clarity of the Rule condition.
3.6. Error texts for Rules
Each Rule has a specified text that is output in the event of an error (error text). Since the Document
Model is not permitted to specify the format of the text, line breaks are not allowed when
specifying the error text. The text is always displayed on a matching interface. The error text may
make use of parameters, which help to generate error messages as precisely as possible.
The labels that are used to display a Field on a user interface can differ from the Field names that
are maintained in the Model. For this reason, it can be useful to use a Field’s name as a parameter
because this allows the proper label to be displayed on the user interface.
If a Rule iterates, then the specific iteration or Field value can be output in the error text through
the use of parameters. Parameters defined in the error text are always enclosed in $ characters. If a
$ character is required in an error text, then $$ must be used.
The following parameters are supported:
Table 11. Error text parameters for Rules
Parameter Description Example
Field.value References the value of the
Field Field. If no value is
specified for Field, then a 0 is
set for numbers and an empty
string for all other Field types.
$Addresses/FirstName.value$
Field -> CategoryName The arrow operator (->) is used
to reference the category
CategoryName for Field Field. If
no value is specified for Field,
an empty string is set.
$Country->AdministrationArea$
index(indexField).value
or
index(indexField) ->
CategoryName
References the value or
category value of the Index
Field indexField. The Index Field
is always specified without the
path name. The current
iteration value is used for
Parallel Iterations, even if some
of the involved Index Fields are
not specified.
$index(Subsidiary).value$
details
37

-- 37 of 261 --

Parameter Description Example
Field References the name of the
Field Field. The Field name can
be replaced by a defined label
to be displayed on the user
interface.
$Subsidiary$
#GroupPath
or
#RuleGroup
References the current iteration
of the Group GroupPath or the
Rule Group.
$#Subsidiaries$
details
semantic index:
… For "StringConstant"
or
… For Field2
With this notation, some
specific iteration can be
addressed, using the semantic
index. The following items can
be used instead of … :
Field
Field.value
Field-> CategoryName
#GroupPath
#RuleGroup
$Addresses/FirstName.value
For "HomeAddress"$
details
#RootGroup References the current iteration
of the root Group which
contains the error Field.
$#RootGroup$
BaseYear For models with a Base Year,
this parameter references the
Base Year. For models without a
Base Year, this parameter is not
permitted.
$BaseYear$
General requirements
• $<Field>.value$ may only be used if the Field is referenced in the error condition at least once
without asterisk operator.
The Fields and Groups inside the Error Text Parameters can be referenced both by relative path
and absolute path.
NOTE The paths of the Error Text Parameters may not contain any asterisks.
NOTE For a Field of type Enumeration which 'Text' specified, $field.value$ returns the Text
rather than the Value.
Example
Consider the Model
38

-- 38 of 261 --

with Rule Condition
[M/Amount] < 0
The following are examples of valid Error Text Parameters:
• $#/A/Investors$ (absolute path)
• $#..$ (relative path)
• $/A/Investors/L/M/Amount$ (absolute path)
• $M/Amount$ (relative path)
The parameters #GroupPath and #RuleGroup return the current repetition of the respective
Group. The following conditions must be fulfilled:
• GroupPath is the path of a Group.
• The Group which contains the Rule can be referred to as $#RuleGroup$.
• The Group has to be repeatably referenced in the Rule condition. For example: if the Rule
condition is NoFieldFilled(Group1/Group2*) then it is not permitted to use the parameter
$#Group1/Group2$.
• If Parallel Iteration is specified for the Group which corresponds to GroupPath, the Rule
condition must assure that it can only be valid if at least one Field of the specified repetition is
filled.
◦ Example 1. GroupsNotCollectivelyFilled(k1, k2) with Parallel Iteration for k1 and k2
Parameters $#k1$ and $#k2$ are not permitted; for example, using $#k1$ results in the
following error:
The parameter '#k1' is not allowed, because the Rule specifies Parallel
Iteration for this level and the error can also occur,
when no Field of the specified repetition is filled.
39

-- 39 of 261 --

◦ Example 2. GroupFilled(k1) And GroupNotFilled(k2) with Parallel Iteration for k1 and k2
Parameter $#k1$ is permitted, parameter $#k2$ is not permitted
◦ Example 3. FieldFilled(k1/k/F) and FieldNotFilled(k2/k/F) with Parallel Iteration for k1
and k2
Parameter $#k1$ is permitted, parameter $#k2$ is not permitted
• The parameter #RootGroup is not allowed if the Rule specifies Parallel Iteration.
Index Fields And Parallel Iteration
Consider the model
with Rule Condition
FieldsNotCollectivelyFilled(K/Participant,L/Participant)
and Error Text
Participant $index(Participant).value$ appears only once.
For the document
Field Instance Value
K[1]/Participant 1
K[2]/Participant 2
L[1]/Participant 2
L[2]/Participant 3
L[3]/Participant 4
40

-- 40 of 261 --

the Rule fires three times, returning:
• Participant 1 appears only once.
• Participant 3 appears only once.
• Participant 4 appears only once.
If only one of the Fields K/Participant and L/Participant has a value, the Error Text Parameter
$index(Participant).value$ picks that value.
Parameters with semantic index
• A Group may only be used with semantic index if it directly contains the Index Field, because
only then the repetition depends on the semantic index. Moreover, the semantic index of the
error text must also be used in the Rule condition (either for the Group or for some Field
contained in that Group).
• A semantic index in an error text parameter is only permitted if the Rule condition can only
hold if at least one Field of the specified repetition is filled.
◦ Example 1. GroupNotFilled(k1 For F)
Parameter $#k1 For F$ is not permitted.
◦ Example 2. GroupFilled(k1 For F) And GroupNotFilled(k2 For F)
Parameter $#k1 For F$ is permitted, parameter $#k2 For F$ is not permitted.
◦ Example 3. FieldFilled(k1/k/F For "value") And FieldNotFilled(k2/F For "value") where
the Index Fields are contained in k1 and k2
Parameter #k1 For "value"$ is permitted, parameter $#k2 For "value"$ is not permitted.
• Fields which are used in error text parameters together with a semantic index must be
referenced with the same semantic index in the error condition.
General example for the use of parameters in error texts
A Rule in Model Employees with Rule condition
AllFieldsFilled(LastName, FirstName, EndEmployment) And FieldNotFilled(BeginEmployment)
error text
For $FirstName.value$ $LastName.value$, $EndEmployment.value$ was specified as the
employment end date. Therefore, the employment begin date must also be specified (country
$index(Country).value$, subsidiary $index(Subsidiary).value$).
and data set
41

-- 41 of 261 --

returns the following error text:
For Kenji Ono, 01/01/2020 was specified as the employment end date. Therefore, the
employment begin date must also be specified (country Japan, subsidiary Osaka).
4. Operators and language constructs
A Rule condition consists of language constructs (including their arguments), operators, and
constants. The possible arguments of a language construct are Fields, Field lists, Groups, Group lists,
values, and value lists. These are specified behind the language construct in round brackets. The
arguments that are permitted for each language construct are clearly defined. Only Fields and
Groups defined in the Model can be referenced.
4.1. Logical operators
A Rule condition can consist of several partial conditions that are combined using the logical
operators And and Or.
Table 12. Logical operators
Symbol Meaning
And And-Link
Or Or-Link
If a condition uses both And and Or, then round brackets must be used in order to clarify how the
combination of And and Or is to be understood. For example, we cannot use a Rule like
[CustomerID]==7 And FieldNotFilled(Amount) Or FieldFilled(Category)
42

-- 42 of 261 --

Instead, we may either use the condition
( [CustomerID]==7 And FieldNotFilled(Amount) ) Or FieldFilled(Category)
or else we may use the condition
[CustomerID]==7 And ( FieldNotFilled(Amount) Or FieldFilled(Category) )
Note that it makes a big difference which of the two options is chosen.
4.2. The Field value operator
Square brackets [ ] are used to access the value of a Field. In this situation, a Field reference may
not contain an asterisk.
In the example
[Hotel/CustomerID]==7 And ( FieldNotFilled(Hotel/Amount) Or [Hotel/Amount] > 0 )
the values of Fields Hotel/CustomerID and Hotel/Amount are selected using the Field value operator.
NOTE For Fields of type Enumeration, the Field value operator picks the enumeration
value, not the display text.
For Fields that lie in a Group containing an Index Field, the Field value operator can also be used
together with the semantic index:
[Addresses/Country For PreferredAddress] == "Switzerland"
For category-based enumeration Fields, the value of a category can be accessed using the ->
operator.
The input after -> must be a valid category of the specified Field.
Example of how to use categories in a Rule condition
If another enumeration Field Country is found in the Group Address of Model Account
43

-- 43 of 261 --

and the enumeration Field Country has a category labeled Administration Area
Value AdministrationArea
Germany EU
Poland EU
USA USA
then the Rule condition
[PaymentType]=="DirectDebit" and [Country -> AdministrationArea] != "EU"
states that direct debit was selected as the payment method for a non-EU country. If there were a
large selection of countries, this condition would not be as easily formulated without the use of
categories.
4.3. Arithmetic operations
4.3.1. Overview of arithmetic operations
The following arithmetic operations are supported:
Table 13. Arithmetic operations
Symbol Meaning
+ Addition
44

-- 44 of 261 --

Symbol Meaning
- Subtraction
* Multiplication
/ Division
^ Power Operator
A numerical term which contains several arithmetic operations is evaluated as follows:
• first, the Power Operator is executed
• then, Multiplication and Division are executed
• finally, Addition is executed
Thus
3 ^ 2 + 1 * 2 = 9 + 2 = 11
Arithmetic expressions can be bracketed arbitrarily by using curley brackets { }. For example,
3 ^ { 2 + 1 } * 2 = 3 ^ 3 * 2 = 27 * 2 = 54
NOTE The Power Operator may not be nested without parentheses. For example, a term
like a ^ b ^ c is not permitted.
4.3.2. Accuracy of arithmetic operations
Arithmetic operations are internally calculated with an arithmetic precision of 19 decimal digits.
NOTE
The Java runtime system bases its calculations on the standard class BigDecimal
with a scale of 19. The numeric values of a data set that need to be validated are not
permitted to have more than 15 digits. This is verified before each validation.
4.3.3. Treatment of division by zero
Since a Field value can be specified as the divisor in a division operation, division by zero can occur
if no Rule exists that ensures a non-zero value was given. To avoid this illegal arithmetic operation,
the operation is not executed in this case. Instead, the comparison in which this calculation is
contained always evaluates to false, i.e. never results in an error.
4.3.4. Examples for using the Power Operator
For the Power Operator a ^ b, the base a may be an arbitrary numerical term. The exponent b must
be a numerical term without decimal places.
Examples:
45

-- 45 of 261 --

a ^ 3 = a * a * a
a ^ 0 = 1
a ^ -3 = 1 / { a ^ 3 } = 1 / { a * a * a }
The last example shows that the power operator a ^ b is not defined for the case "a = 0 and b < 0".
In this case, the operation is not executed. This corresponds to the behavior for division by zero.
Moreover, the operation a ^ b is not executed if b is smaller than -1000 or greater than 1000.
For Number Fields F and G without Decimal Places,
Ex. F G [F] ^ [G] Rule with condition [F] ^ [G] > 10
i) 2 4 16 fires
ii) 0 -2 not executed does not fire
iii) 2 1001 not executed does not fire
It is therefore not immediately apparent whether the rule does not fire because the rule condition
is not satisfied, or because the power operator is not evaluated for the 'too large' exponent 1001.
To eliminate such ambiguities, one could specify
• Min. Value = -1000
• Max. Value = 1000
for the Field G.
WARNING
When using the power operator a ^ b, situations like
a = 0 and b < 0
b is smaller than -1000
b is larger than 1000
should be avoided (by means of Field properties or validation rules), because
otherwise a ^ b is not executed and rules which use the term a ^ b may
behave unexpectedly.
4.4. String operations
The joining of strings is also known as the concatenation of strings. Equivalent to the addition of
numbers, this operation is notated using the + symbol.
The condition
[FirstName] + " " + [LastName] == "Angela Huber"
46

-- 46 of 261 --

is satisfied if Field FirstName contains the value Angela and Field LastName contains the value
Huber.
It is important to note that if a date is used to specify a string value, e.g. "30.11", it is interpreted as
a date constant and not a string constant.
For example, let’s assume a Field YearString of type string exists. The condition
"30.11." + [YearString] + "!" == "30.11.2021!"
is not valid because "30.11." is not a string constant and concatenation is therefore not possible.
On the other hand, the condition
"30."+"11." + [YearString] + "!" == "30.11.2021!"
can be used.
For an example which includes line breaks, consider the condition
[Address] == [FirstName] + " " + [Surname] + "
" + [Street] + " " + [HouseNumber]
where Address is a Field of type string with line breaks permitted and the other Fields are string
Fields without line breaks permitted. This condition is true for the document
Field Value
Address Annalena Müller
Taunusstr. 23
FirstName Annalena
Surname Müller
Street Taunusstr.
HouseNumber 23
Thus, a line break can be modelled in terms of the string constant
"
"
NOTE
In the kernel language, line breaks are technically represented by a single line feed
\n. Therefore, the evaluation of multi line input Fields is independent of the local
operating system.
47

-- 47 of 261 --

4.5. Comparison operations
Table 14. Comparison operations
Symbol Meaning
== equal
!= unequal
> greater than
< less than
>= greater than or equal to
<= less than or equal to
[Field1] DiffersWithToleranceRange10 [Field2] Field1 is unequal to Field2 with a tolerance of 10,
i.e. the absolute value of [Field1] minus [Field2]
is greater than 10.
[Field1] DiffersWithToleranceRange5 [Field2] Field1 is unequal to Field2 with a tolerance of 5,
i.e. the absolute value of [Field1] minus [Field2]
is greater than 5.
[Field1] DiffersWithToleranceRange2 [Field2] Field1 is unequal to Field2 with a tolerance of 2,
i.e. the absolute value of [Field1] minus [Field2]
is greater than 2.
[Field1] DiffersWithToleranceRange1 [Field2] Field1 is unequal to Field2 with a tolerance of 1,
i.e. the absolute value of [Field1] minus [Field2]
is greater than 1.
This section covers the comparison of numbers and strings. See Date Fragments for comparing Date
(Fragment) values and Comparing Date Ranges for comparing Date Ranges.
4.5.1. Comparing numbers
Fields of type Number can be more precisely specified using the maximum number of decimal
places attribute. By default, the comparison operations == and != are not permitted between two
numbers whose maximum number of decimal places do not match. In this case, a rounding
operation must be specified.
NOTE
Exceptions exist, such as the evaluation of a checking number, where you may wish
to forego a rounding operation. This can be achieved by using the hint
@SuppressWarning(MVK_INVALID_COMPARE_DEC_PLACES)
at the beginning of the Rule condition.
Additionally, the maximum number of decimal places can increase during multiplication, as can be
seen in the calculation:
0.1 * 0.1 == 0.001
48

-- 48 of 261 --

Let’s assume that the Fields Product, Factor1, and Factor2 have a maximum number of 3 decimal
places each. In this case, the comparison
[Product] == [Factor1] * [Factor2]
is not valid.
To be able to execute this comparison, the maximum number of decimal places must be adjusted
through the use of rounding operations:
[Product] == RoundAccounting([Factor1] * [Factor2],3)
4.5.2. Comparing strings and enumerations
A Field of type enumeration is based on enumeration values ('Value'). Optionally, for each locale of
the Model and each enumeration value of the Field, a localized display text ('Text') can be specified.
Example
For an enumeration Field Country with
Enumeration value Text(en) Text(de)
F France Frankreich
I Italy Italien
the enumeration value is used in Rule conditions, as in
[Country]=="F"
whereas the localized display text are used to display the choice option on the screen. Thus, when
choosing France on the screen , the Field Country has the value F in the document.
NOTE
When comparing an enumeration Field with a string Field with == or !=, the
enumeration value of the enumeration Field is compared with the value of the
string Field. The modeler should bear in mind that the end user sees the display text
and does not know the enumeration value.
NOTE
It is not possible to check whether a string Field is filled by using conditions like
[F]=="" or [F]!="". Regardless of whether a value is specified, [F]=="" or [F]!="" are
always evaluated as false. To check whether a Field is filled , use the language
constructs FieldFilled, FieldNotFilled respectively.
49

-- 49 of 261 --

4.6. Predicate language constructs
Predicate language constructs are formal statements that, depending on their arguments, are
either satisfied or not. Predicate language constructs can be used as Rule conditions or partial
conditions of a Rule condition.
FieldNotFilled(FirstName)
The condition is satisfied if Field FirstName is not specified.
Predicate language constructs are discussed separately for individual Fields, Field lists, and Groups
in the following chapters.
4.6.1. Predicate language constructs for individual Fields
Predicate language constructs for individual Fields accept either a single Field or a single Field in
combination with a value list as its arguments.
Table 15. Predicate language constructs for individual Fields
Language construct with argument(s) Condition satisfied
FieldFilled(Field) A value is given for Field. (details)
FieldNotFilled(Field) A value is not given for Field. (details)
FieldValueIncludedInValueList(Field,
ValueList)
A value is given for Field that is included in the
ValueList; here, ValueList is either a list of
constants or a list of Field values like [F1], [F2],
[F3]. (details)
FieldValueNotIncludedInValueList(Field,
ValueList)
A value is given for Field that is not included in
the ValueList; here, ValueList is either a list of
constants or a list of Field values like [F1], [F2],
[F3]. (details)
FieldFilled and FieldNotFilled
The condition FieldFilled(Field) is satisfied if a value is given for Field Field. This construct is
unlikely to occur on its own. However, this operation is used frequently in combination with other
language constructs. For example, this language construct can be used to ensure that referenced
Fields in a comparison are specified.
In many cases, one might prefer to use the condition
FieldFilled(Amount) And [Amount] == 0
instead of
50

-- 50 of 261 --

[Amount] == 0
Additionally, the language construct FieldFilled is used to specify dependent Fields, such as when
one Field requires another Field to be given as well.
FieldFilled(FirstName) And FieldNotFilled(SecondName)
The condition FieldNotFilled(Field) is satisfied if no value is specified for a Field.
FieldValueIncludedInValueList and FieldValueNotIncludedInValueList
FieldValueIncludedInValueList(Field, ValueList) is satisfied if the value of Field is found in the
value list. If no value is specified for Field or the specified value is not present in the value list, then
the Rule condition is not satisfied.
FieldValueNotIncludedInValueList(Field, ValueList) is satisfied if the value specified for Field is not
found in the value list. If no value is specified for Field or the value for Field is present in the value
list, then the Rule condition is not satisfied.
FieldValueIncludedInValueList and FieldValueNotIncludedInValueList can be used for String,
Number and Enumeration Field types.
The following criteria apply to the value list:
• the list must consist of either a list of Field values (such as [Field1], [Field2], [Field3]) or a list of
string or number constants
• if it consists of a list of Field values:
◦ Field Field cannot be a value in the value list
◦ all Fields must have the same type as Field Field
• if it consists of a list of string constants and Field has type Enumeration, then only valid
enumeration values can be specified, i.e. only values that can be specified for Field or valid
category values in case category access is specified (by the operator ->)
Example (we assume that ProductNumber is of type string):
FieldValueNotIncludedInValueList(ProductNumber, "007", "42", "4711")
The Rule condition is true if a product number is specified but the value is neither "007", "42"
nor "4711", that means it is a shorter way of formulating:
FieldFilled(ProductNumber) And [ProductNumber] != "007" And [ProductNumber] != "42" And
[ProductNumber] != "4711"
As an alternative to checking the Field value, category values can also be evaluated in an
enumeration Field with categories.
51

-- 51 of 261 --

Field Product is an enumeration with category ProductClassification
FieldValueNotIncludedInValueList(Product -> ProductClassification, "Food", "Electronic")
4.6.2. Predicate language constructs for Field lists
Predicate language constructs for Field lists accept either a Field list or a Field list combined with a
value list as its arguments. The predicate language constructs for Field lists are described in Table
16.
Table 16. Predicate language constructs for Field lists
Language construct with argument(s) Condition satisfied
AllFieldsFilled(FieldList) For all Fields in FieldList a value is given. (
details)
NoFieldFilled(FieldList) No value is given for any of the Fields in
FieldList. (details)
AtLeastOneFieldFilled(FieldList) For at least one Field in FieldList a value is given.
(details)
NotExactlyOneFieldFilled(FieldList) Either none or else more than one of the Fields
in FieldList are filled. (details)
MoreThanOneFieldFilled(FieldList) For more than one Field in FieldList a value is
given. (details)
NotAllFieldsFilled(FieldList) A value is not given for all Fields in FieldList.
(details)
FieldsNotCollectivelyFilled(FieldList) A value is given for at least one Field but not all
Fields in FieldList. (details)
FieldValuesNotUnique(FieldList) At least two Fields in FieldList share the same
value. (details)
NoFieldValueIncludedInValueList(FieldList IN
ValueList)
No Field value in FieldList is contained in
ValueList; here, ValueList is either a list of
constants or a Field list. (details)
AtLeastOneFieldValueIncludedInValueList(FieldL
ist IN ValueList)
One or more Field values in FieldList are
included in ValueList; here, ValueList is either a
list of constants or a Field list. (details)
NotAllFieldValuesIncludedInValueList(FieldList
IN ValueList)
For at least one Field in FieldList, a value is given
but this value is not included in ValueList; here,
ValueList is either a list of constants or a Field
list. (details)
RepetitionNotUnique(FieldList @From Group) There are at least two instances of the Rule
iteration for which the Fields in FieldList have
the same values. The additional specification
'@From Group' is optional. (details)
52

-- 52 of 261 --

AllFieldsFilled
The condition AllFieldsFilled(FieldList) is satisfied if a value is specified for all Fields in FieldList.
NoFieldFilled
The condition NoFieldFilled(FieldList) is satisfied if no value is specified for any of the Fields in
FieldList.
AtLeastOneFieldFilled
The condition AtLeastOneFieldFilled(FieldList) is satisfied if at least one Field value is specified in
FieldList.
NotExactlyOneFieldFilled
The condition NotExactlyOneFieldFilled(FieldList) is satisfied if either no Field or more than one
Field value is specified in FieldList.
NOTE
NotExactlyOneFieldFilled(FieldList) is equivalent to
NumberOfFilledFields(FieldList) != 1.
However, the evaluation of NotExactlyOneFieldFilled is considerably faster because
it can be canceled if two values are specified. For NumberOfFilledFields, the
evaluation begins by checking if all Fields in FieldList are specified and goes on to
compare the total number of Fields found with a constant value of 1.
MoreThanOneFieldFilled
The condition MoreThanOneFieldFilled(FieldList) is satisfied if a value was specified for more than
one Field in FieldList.
NotAllFieldsFilled
The condition NotAllFieldsFilled(FieldList) is satisfied if at least one value is not specified for a
Field in FieldList.
[PaymentType]=="CreditCard" And NotAllFieldsFilled(CreditCard)
FieldsNotCollectivelyFilled
The condition FieldsNotCollectivelyFilled(FieldList) is satisfied if at least one, but not all Fields in
FieldList a value is specified.
Example ('FieldsNotCollectivelyFilled' in combination with the asterisk operator)
Consider the Model
53

-- 53 of 261 --

with Rule condition
FieldsNotCollectivelyFilled(K*/StartDate)
The Rule R fires if both of the conditions
• for at least one repetition of K the Field StartDate is filled
• for at least one repetition of K the Field StartDate is not filled
are true. Note, however, that all 99 repetitions of the Group K are checked, not only those who have
any values. For example, with the document
K Id StartDate
[1] 25 11.02.2023
[2] 12.03.2023
[3] 13 13.04.2023
[4] 14.05.2023
the Rule does fire.
FieldValuesNotUnique
FieldValuesNotUnique(FieldList) is satisfied if at least two Fields in FieldList have the same value. If
no values have been specified, the condition is not satisfied.
NOTE
In many cases, it can be advantageous to use the language construct
RepetitionNotUnique instead of FieldValuesNotUnique because the Rule then iterates
and the subsequent feedback indicates the iteration in which the duplication
occurred. The condition
FieldValuesNotUnique(Order/Products*/ProductNumber)
ensures that no product number occurs more than once in the product list. It does
not indicate in which instance the error occurs. Therefore, the condition
RepetitionNotUnique(Order/Products/ProductNumber)
54

-- 54 of 261 --

is better: This Rule returns an error for every instance in which a product number
was given that exists in another instance. This allows the error to be clearly
identified.
NoFieldValueIncludedInValueList
NoFieldValueIncludedInValueList(FieldList IN ValueList)is satisfied if no Field value in FieldList is
contained in ValueList. In particular, this condition is also satisfied if no values for FieldList are
specified or if no values for ValueList are specified.
NoFieldValueIncludedInValueList can only be used with Fields of type String, Number, and
Enumeration.
The ValueList is either a Field list or a list of string constants.
If ValueList is a Field list, then the following also applies:
• the Fields may not be included in the evaluated FieldList
• the Fields must have the same type as the Fields contained in the evaluated FieldList
If the Fields of FieldList are of type enumeration and ValueList is a list of string constants, then each
of the string constants must occur as enumeration value of one of the Fields in FieldList or as
category value in case category access is specified (by the operator ->).
NoFieldValueIncludedInValueList(Products*/ProductNumber IN "007", "42", "4711", "0815")
This Rule condition verifies that none of the product numbers are in the list 007, 42, 4711, 0815.
AtLeastOneFieldValueIncludedInValueList
AtLeastOneFieldValueIncludedInValueList(FieldList IN ValueList) is satisfied if at least one value of
a Field in FieldList is also included in ValueList.
AtLeastOneFieldValueIncludedInValueList can only be used with Fields of type String, Number and
Enumeration.
The ValueList is either a Field list or a list of string constants.
If the ValueList is a Field list, the following also applies:
• the Fields may not be included in the evaluated FieldList
• the Fields must have the same type as the Fields contained in the evaluated FieldList
If the Fields of FieldList are of type enumeration and ValueList is a list of string constants, then each
of the string constants must occur as enumeration value of one of the Fields in FieldList or as
category value in case category access is specified (by the operator ->).
55

-- 55 of 261 --

NotAllFieldValuesIncludedInValueList
NotAllFieldValuesIncludedInValueList(FieldList IN ValueList) is satisfied if at least one value in
FieldList is not included in ValueList.
The following combinations can be used as arguments for this language construct:
FieldList ValueList
Fields or Field instances of type number (a
single Field is also possible)
Fields or Field instances of type number (a
single Field is also possible)
Fields or Field instances of type string (a single
Field is also possible)
Fields or Field instances of type string (a single
Field is also possible)
Fields or Field instances of type string (a single
Field is also possible)
List of string constants (a single string is also
possible)
Fields or Field instances of type enumeration (a
single Field is also possible)
Fields or Field instances of type enumeration (a
single Field is also possible)
Fields or Field instances of type enumeration (a
single Field is also possible)
List of string constants (a single string is also
possible); each of the string constants must
occur as an enumeration value of a Field in
FieldList or as category value in case category
access is specified (by the operator ->).
RepetitionNotUnique
The condition RepetitionNotUnique(FieldList @From Group) iterates according to the Fields given in
FieldList. For each instance of the iteration, the condition is true if there exists another (different)
instance with the same values in FieldList.
NOTE By @From Group, a reference Group is specified, based on which (from which on)
uniqueness is checked.
If the basic version
RepetitionNotUnique(FieldList)
is used, the reference Group depends on the Rule Group (cf. basic version).
The following criteria apply to the FieldList:
• it only contains Fields (no Groups)
• the arguments do not contain an asterisk
• all Fields in the Field list must lie on the same path
Consider the Model
56

-- 56 of 261 --

Example 1
We want to specify a Rule which shows errors if the combination of OrderDate and ProductNumber
is not unique.
Given document
Order OrderDate Products ProductNumber
[1] 12.07.2021 [1] 101
[2] 100
[2] 23.07.2021 [1] 101
[2] 68
[3] 101
the Rule should fire for the iterations
• Order[2]/Products[1]
• Order[2]/Products[3]
Given document
Order OrderDate Products ProductNumber
[1] 23.07.2021 [1] 101
[2] 100
[2] 23.07.2021 [1] 101
[2] 68
[3] 101
the Rule should fire for the iterations
• Order[1]/Products[1]
57

-- 57 of 261 --

• Order[2]/Products[1]
• Order[2]/Products[3]
The Rule
Rule Group Rule condition
Account RepetitionNotUnique(Order/OrderDate, Order/Products/ProductNumber @From Order)
fulfills this requirement. There are several other ways to specify this Rule:
Rule Group Rule condition
Order RepetitionNotUnique(OrderDate, Products/ProductNumber @From RuleGroup)
Products RepetitionNotUnique(../OrderDate, ProductNumber @From ..)
Products RepetitionNotUnique(../OrderDate, ProductNumber @From /Account/Order)
Example 2
We want to specify a Rule which shows errors if the values of ProductNumber are not unique.
Given document
Order Products ProductNumber
[1] [1] 101
[2] 100
[2] [1] 101
[2] 68
[3] 101
the Rule should fire for the iterations
• Order[1]/Products[1]
• Order[2]/Products[1]
• Order[2]/Products[3]
The Rule
Rule Group Rule condition
Account RepetitionNotUnique(Order/Products/ProductNumber @From Order)
fulfills this requirement. There are several other ways to specify this Rule.
Rule Group Rule condition
Order RepetitionNotUnique(Products/ProductNumber @From RuleGroup)
Products RepetitionNotUnique(ProductNumber @From ..)
Products RepetitionNotUnique(ProductNumber @From /Account/Order)
58

-- 58 of 261 --

Example 3
Now we want to specify a Rule which shows errors if, for fixed iteration of Order, the values of
ProductNumber are not unique.
Given document
Order Products ProductNumber
[1] [1] 101
[2] 100
[2] [1] 101
[2] 68
[3] 101
the Rule should fire for the iterations
• Order[2]/Products[1]
• Order[2]/Products[3]
The Rule
Rule Group Rule condition
Account RepetitionNotUnique(Order/Products/ProductNumber @From Order/Products)
fulfills this requirement. There are several other ways to specify this Rule.
Rule Group Rule condition
Order RepetitionNotUnique(Products/ProductNumber @From Products)
Products RepetitionNotUnique(ProductNumber @From RuleGroup)
The basic version
When using the basic version RepetitionNotUnique(FieldList) , like in
Rule Group Rule condition
Account RepetitionNotUnique(Order/OrderDate, Order/Products/ProductNumber)
Account RepetitionNotUnique(Order/Products/ProductNumber)
Order RepetitionNotUnique(Products/ProductNumber)
one of the following situations must be given.
Situation 1
• The Rule Group G is a repeatable root Group and
• all of the Fields of FieldList are directly contained in G.
In this case
59

-- 59 of 261 --

• RepetitionNotUnique(FieldList)
• RepetitionNotUnique(FieldList @From RuleGroup)
lead to the same validation behaviour.
NOTE When RepetitionNotUnique is used like in Situation 1, the reference Group is the
Rule Group.
Situation 2
There exists a repeatable Group G which
• is contained in the Rule Group and
• contains all Fields of FieldList.
In this case
• RepetitionNotUnique(FieldList)
• RepetitionNotUnique(FieldList @From RelativePathOf_G)
lead to the same validation behaviour.
NOTE When RepetitionNotUnique is used like in Situation 2, the reference Group is the first
repeatable Group below the Rule Group.
Example 4
Given Model
the Rule
Rule Group Rule condition
Payments RepetitionNotUnique(General/Amount, General/Due @From /Payments)
which can also be specified in the varieties
60

-- 60 of 261 --

Rule Group Rule condition
Payments RepetitionNotUnique(General/Amount, General/Due @From RuleGroup)
General RepetitionNotUnique(Amount, Due @From /Payments)
General RepetitionNotUnique(Amount, Due @From ..)
shows errors if the combination of Amount and Due is not unique.
NOTE
The Rule of Example 4 cannot be specified by means of the basic version
RepetitionNotUnique(FieldList), because the Fields Amount and Due are not directly
contained in the root Group Payments.
RepetitionNotUnique and non-specified values
If for a Field in the FieldList in
• RepetitionNotUnique(FieldList) or
• RepetitionNotUnique(FieldList @From Group)
a value is specified for a certain repetition and no value is specified for another repetition, then
these repetitions are considered to be different. Thus, the Rule in
does not fire.
4.6.3. Choosing the right 'ValueList'-language construct
The 'ValueList'-language constructs can be divided into
Category 1
• FieldValueIncludedInValueList(<Field>, <ValueList>)
• FieldValueNotIncludedInValueList(<Field>, <ValueList>)
and
Category 2
• NoFieldValueIncludedInValueList(<FieldList> IN <ValueList>)
61

-- 61 of 261 --

• AtLeastOneFieldValueIncludedInValueList(<FieldList> IN <ValueList>)
• NotAllFieldValuesIncludedInValueList(<FieldList> IN <ValueList>)
There are a few differences between the two categories.
Category 1 Category 2
complexity clear and simple more powerful but also more
complex
the first argument is a Field is a Field list (note that a Field
list can also consist of a single
Field)
the two arguments are
separated by
a comma IN
the second argument is either a list of constants or a
list of Field values like
[F1],[F2],[F3]
is either a list of constants or a
Field list
In most cases, the language constructs of Category 1 can be expressed by language constructs of
Category 2. In some situations, this is even necessary. Consider the Model
Example 1
The Rule R should fire if
• a value is given for G
• this value of G equals the value of F for some repetition of K.
A natural choice for a Rule condition would be
FieldValueIncludedInValueList(G, K*/F)
However, the Field list K*/F is not permitted as a second argument for
FieldValueIncludedInValueList. Instead, the condition
62

-- 62 of 261 --

AtLeastOneFieldValueIncludedInValueList(G IN K*/F)
can be used.
Example 2
The Rule R should fire if
• a value is given for G
• the value of G does not equal the value of F for any repetition of K.
A natural choice for a Rule condition would be
FieldValueNotIncludedInValueList(G, K*/F)
However, the Field list K*/F is not permitted as a second argument for
FieldValueNotIncludedInValueList. Instead, the condition
NotAllFieldValuesIncludedInValueList(G IN K*/F)
can be used here. Note that this condition can only be true if G is filled.
4.6.4. Predicate language constructs for Groups
The language constructs GroupFilled and GroupNotFilled each has exactly one Group as an
argument. All other language constructs accept multiple arguments and can reference a mix of
Fields and Groups. However, using Fields should remain an exception. A Field that is used as an
argument is interpreted as a Group that only contains this Field. Except when used in language
constructs GroupNotFilled and AtLeastOneGroupFilled, rootGroups are only allowed to be used as
single arguments in NoGroupFilled and AtLeastOneGroupFilled.
A Group or Field may not be used as an argument more than once. In particular, an explicitly
referenced Field may not be indirectly referenced via a Group.
A Group corresponds to the list of all of its Fields. A Group is considered specified if at least one of
its Fields is specified, either directly or indirectly.
Table 17. Predicate language constructs for Groups
Language construct with argument(s) Rule condition satisfied (Rule returns an
error)
GroupFilled(Group) For at least one Field of Group a value is given.
(details)
GroupNotFilled(Group) No value is given for any Field of Group. (details)
NoGroupFilled(GroupList) No Group in GroupList contains a Field that is
filled. (details)
63

-- 63 of 261 --

Language construct with argument(s) Rule condition satisfied (Rule returns an
error)
AtLeastOneGroupFilled(GroupList) There is a Group in GroupList that contains a
Field which is filled. (details)
NotAllGroupsFilled(GroupList) There is a Group in GroupList that is not filled.
(details)
GroupsNotCollectivelyFilled(GroupList) At least one but not all Groups in GroupList are
filled. (details)
AllGroupsFilled(GroupList) Each Group in GroupList is filled. (details)
GroupFilled and GroupNotFilled
GroupFilled and GroupNotFilled must use exactly one Group as an argument. The argument is not
allowed to have an asterisk.
FieldNotFilled(AlternativeDeliveryAddress) And GroupFilled(DeliveryAddress)
• GroupFilled(Group) is valid if at least one of the Fields of Group is filled.
• GroupNotFilled(Group) is valid if none of the Fields of Group is filled.
• If Group is not repeatable, GroupFilled(Group) is only valid if at least one the Fields of Group is
filled.
• If a Group is repeatable, GroupFilled(Group) is also valid for empty rows of a repeat that have
been created.
NOTE New rows may be created either by the end-user clicking on the "add" button or
by the Modeler adding initial rows to a repeat in the Form Model.
Repeatable Groups are displayed on the user interface as repeats. For example, the repeatable
Group Products in
64

-- 64 of 261 --

might be represented like this:
The first row of the inline repeat represents the first repetition of the Group Products and the
second row of the inline repeat represents the second repetition of the Group Products.
A Rule with error condition
GroupFilled(Products) And FieldNotFilled(Products/ProductNumber)
iterates over the repetitions of Products, the rows of the repeat respectively. An error message is
displayed also for empty (for example freshly created) rows.
Thus, for the corresponding repetition 3 of Products, GroupFilled(Products) is valid even though
no Field of this repetition is filled.
NoGroupFilled and AtLeastOneGroupFilled
NoGroupFilled and AtLeastOneGroupFilled can reference multiple different Fields and Groups, but
only one rootGroup.
NOTE
NoGroupFilled(GroupList)
is equivalent to
NoFieldFilled(GroupList)
65

-- 65 of 261 --

and
NoFieldFilled(all Fields in GroupList)
NoGroupFilled(Products)
NotAllGroupsFilled, GroupsNotCollectivelyFilled and AllGroupsFilled
NotAllGroupsFilled , GroupsNotCollectivelyFilled , and AllGroupsFilled can reference multiple
Fields and Groups. A rootGroup may not be used as an argument. The arguments may not use an
asterisk.
NOTE
AllGroupsFilled(GroupList)
is equivalent to
AtLeastOneFieldFilled(all Fields of Group1) and AtLeastOneFieldFilled(all Fields
of Group2) and … AtLeastOneFieldFilled(all Fields of Group-n)
GroupsNotCollectivelyFilled(GroupList) is satisfied if at least one Field value is specified in the
Group list, but not every Group has at least one specified Field.
GroupsNotCollectivelyFilled(Addresses, Order)
4.7. Functional language constructs
Functional language constructs return a value, which can be used as argument of other language
constructs.
NumberOfFilledFields(Products*/ProductNumber) >= 5
The condition is satisfied if a value is specified for at least 5 instances of ProductNumber.
Functional language constructs often return a default value for unspecified Fields (Section 3.5). For
this reason, Modelers should make sure that a Rule condition would not be undesirably fulfilled
through unspecified Fields. If necessary, this can be prevented by adding FieldFilled(Field) as an
additional partial condition.
The functional language constructs for Fields, as well as Field lists and Groups, are described
separately in the following chapters. Although arithmetic language constructs are also functional
language constructs, they are described separately in Section 4.8.
4.7.1. Functional language constructs for single Fields
Table 18 lists the functional language constructs for single Fields. These are used for conversions.
Table 18. Functional language constructs for single Fields
66

-- 66 of 261 --

Language construct with argument(s) Description
FieldValueAsString(Field) Returns the value of Field as a string. (details)
FieldValueAsNumber(Field) Returns the value of Field as a number. (details)
ValueAsDate(Field, DateSpecification) Returns a specific date corresponding to the
DateSpecification of a partially known date.
(details)
RangeAsString(Field, Start, End) Returns the substring of Field given by Start and
End as a string. (details)
RangeAsNumber(Field, Start, End) Returns the substring of Field given by Start and
End as a number. (details)
FieldValueAsString and FieldValueAsNumber
FieldValueAsString(Field) returns the value of Field as a string. Field must have type Number. An
empty string is returned if no value is specified for Field. Subsequent comparisons are
alphanumeric.
FieldValueAsString(Discount) == "100000"
FieldValueAsNumber(Field) returns the value of Field as a number. To ensure that the value of Field
can be converted to a number, only Fields of type String with pattern [0-9]+ and Enumeration
containing numerical values are allowed. The maximal length for each Field must be less than 16. If
no value is specified for Field, the value 0 is returned. Subsequent comparisons are numerical.
FieldFilled(ProductNumber) And FieldValueAsNumber(ProductNumber) <= 0
FieldValueAsNumber(ProductNumber)<=0 is satisfied if the value of ProductNumber is less than or
equal to 0. The first partial condition ensures that the Rule condition does not apply if no value
is specified for ProductNumber.
FieldValueAsNumber in combination with the access operator for categories
If F is a Field of type enumeration with a category K such that the category values of K can be
converted into numbers (possibly with decimal places), then the access operator for categories F->
K can also be used as an argument for FieldValueAsNumber.
For example, let Country be a Field of type enumeration with category Multiplicator
Field 'Country'
Value Multiplicator
France 10.5
China 20.8
Then a condition like
67

-- 67 of 261 --

FieldValueAsNumber(Country -> Multiplicator) < 15
can be used.
RangeAsString and RangeAsNumber
RangeAsString(Field, Start, End) and RangeAsNumber(Field, Start, End) return the characters of
Field from index Start up to and including End as a string or a number. The arguments must consist
of a Field and two numbers. The value of End is not allowed to be smaller than the value of Start.
The Field Field must be of type String.
An empty string is returned for RangeAsString(Field, Start, End) if no value is specified for Field or
if the values given for Start and End do not correlate with the length of Field. Subsequent
comparisons are alphanumeric.
RangeAsString(IBAN, 1, 2) != "DE"
The condition is satisfied if the first two characters of the value of IBAN are not "DE", i.e. it is
not the country code for Germany.
The value 0 is returned for RangeAsNumber(Field, Start, End) if no value is specified for Field, if the
values given for Start and End do not correlate with the length of Field or if the substring defined by
Start and End is not a numerical digit sequence. Subsequent comparisons are numerical.
FieldFilled(CardNumber) And RangeAsNumber(CardNumber, 1, 1) == 0
The condition is satisfied if a value is specified for CardNumber with a leading 0.
NOTE
Strings which contain combined characters. If the chosen character set allows
combined characters like C̨̆ , which is defined by the Unicode code point sequence
(0x0043, 0x0328, 0x0306), then the length of this combined character is given by the
length of the defining sequence. Thus, C̨̆ has length 3. For example, if a Field F of
type string has the value C̨̆e12 , then RangeAsString(F,1,3) returns the string C̨̆ ,
and RangeAsNumber(F,5,6) returns the number 12.
NOTE
When using the language constructs RangeAsString and RangeAsNumber, it is good
modelling practice to secure/constrain the value of a referenced Field with a pattern
— either in the Field definition or by using the language construct PatternViolated.
4.7.2. Functional language constructs for Field lists and Group lists
The functional language constructs for Field lists and Group lists take a Field list or a Group list as
an argument, respectively. The predefined language constructs for Field and Group lists are
described in Table 19.
Table 19. Functional language constructs for Field and Group lists
68

-- 68 of 261 --

Language construct with argument(s) Description
NumberOfFilledFields(FieldList) Returns the number of filled Fields in FieldList.
(details)
NumberOfFilledGroups(GroupList) Returns the number of filled Groups in
GroupList. (details)
NumberOfDifferentValues(FieldList) Returns the number of different values of the
Fields in FieldList. (details)
FirstFilledValue(FieldList) Returns the value of the first filled Field in
FieldList. (details)
NumberOfValueInFields(Constant IN FieldList) Returns the number of Fields in FieldList with
value Constant. (details)
NumberOfFilledFields
NumberOfFilledFields determines the number of specified Fields in a Field list. If a Field list
contains a Group, the Group is replaced by all Fields in the Group. This also applies to all Fields in
the direct and indirect subGroups.
NumberOfFilledFields(Products*/ProductNumber) > 5
This condition is satisfied if more than 5 product numbers have been specified.
NumberOfFilledGroups
NumberOfFilledGroups determines the number of specified Groups in a Group list. A Group is
considered specified if it contains a specified Field or subGroup. SubGroups are not counted
individually; instead, they are used to determine whether a listed Group is specified. If a subGroup
should be counted, it must be specified in the Group list.
NumberOfFilledGroups(Products*) > 5
The condition is satisfied if more than 5 products have been specified.
The Group list
• may only have Groups, i.e. it may not have any Fields
• must have more than one Group or a single Group with an asterisk
• can only list rootGroups separately and therefore always with an asterisk
NumberOfFilledGroups(/Persons*) > 100
The condition is satisfied if more than 100 people are specified. Since Person is a rootGroup, no
other Group may be included in the list.
69

-- 69 of 261 --

NumberOfDifferentValues
NumberOfDifferentValues determines the number of different Field values found in the Field list.
The Fields in the Field list must have comparable types:
• Enumeration and String Field types are comparable.
• Number and String Field types, on the other hand, are not.
This language construct does not support Fields of type confirm and boolean. The values of Field1
and Field2 would be considered different if both Fields are specified, but have different values.
NumberOfDifferentValues(Products*/ProductName) > 5
The condition is satisfied if more than 5 different product names are specified.
FirstFilledValue
FirstFilledValue returns the first specified Field value in the Field list. A Group listed in the Field
list is replaced by all Fields of that Group. This also applies to all Fields of the direct and indirect
subGroups. The Field types used with FirstFilledValue must be comparable:
• String, Enumeration, and Constant can be used together.
• Number Fields can be used with other Number Fields.
• Date Fields must all have the same format.
• DateRange Fields must have the same format.
NOTE This language construct is beneficial in combination with the filter operator.
[Field] != FirstFilledValue(Products*/ProductName Having
[Products/UnitPrice] == MaxValue(Products*/UnitPrice))
The ProductName of the most expensive product can be determined using a filter and
FirstFilledValue.
NumberOfValueInFields
NumberOfValueInFields(Constant IN FieldList) determines the number of Fields in FieldList whose
values are equal to the value of Constant.
The following must hold:
• there must be more than one specified Field in FieldList
• Fields in FieldList are not allowed to be listed multiple times
Only String, Enumeration, Number, Confirm, and Boolean Field types are supported.
The Constant can be one of the following types:
70

-- 70 of 261 --

• String constant
• Number constant
• The constant True
• The constant False
The Fields in the Field list and the Field constant must have matching types. The following types are
considered matching:
• constant type number and Field type number
• constant type String and Field type string or enumeration
• constant True and Field type confirm or boolean
• constant False and Field type boolean
The following must hold:
• If the Field type is Enumeration, all Fields must have the constant value as an enumeration
value or as a category value in case category access is specified (by the operator ->).
If we want to check how often yes was specified in confirm Fields Checkbox1 and Checkbox2:
NumberOfValueInFields(True IN Checkbox1, Checkbox2)
4.7.3. Detecting the current repetition
Language construct with argument(s) Description
CurrentRepetition(Group) Precondition: the Rule iterates over Group or
Group is used with a semantic index.
If the Rule iterates over Group, the operator
returns the respective repetition index of the
Group. If Group is used with a semantic index,
the repetition which corresponds to the value of
the Index Field is returned. (details) (details)
CurrentRepetition
The condition
CurrentRepetition(Addresses)==2 and [Addresses/Type]=="BillingAddress"
is valid if the second repetition of Addresses is the billing address.
The argument of the language construct CurrentRepetition is a Group, which can be specified by a
relative or absolute path. CurrentRepetition can only be used if an additional condition assures that
71

-- 71 of 261 --

the respective repetition is filled. Thus, the condition
CurrentRepetitiion(Addresses)==2
cannot stand on its own.
4.8. Arithmetic language constructs
Arithmetic language constructs are functional language constructs that return a value. This value
can be used as an argument of other language constructs or for calculations.
RoundUpValue(TotalPrice) < 10
Arithmetic language constructs often return a default value for unspecified Fields (Section 3.5). For
this reason, Modelers should make sure that a defined Rule condition would not be undesirably
fulfilled through unspecified Fields. If necessary, this can be prevented by adding FieldFilled(
Field) as an additional partial condition.
The following chapters describe arithmetic language constructs for single Fields and Field lists
separately.
4.8.1. Arithmetic language constructs for single Fields
Table 20 lists the arithmetic language constructs for single Fields.
Table 20. Arithmetic language constructs for single Fields
Language construct with argument(s) Description
RoundDown(Value, DecimalPlaces) Returns Value, rounded down to DecimalPlaces
decimal places. The parameter DecimalPlaces is
optional. (details)
RoundDownValue(Field, DecimalPlaces) Returns the value of Field, rounded down to
DecimalPlaces decimal places. The parameter
DecimalPlaces is optional. (details)
RoundUp(Value, DecimalPlaces) Returns Value, rounded up to DecimalPlaces
decimal places. The parameter DecimalPlaces is
optional. (details)
RoundUpValue(Field, DecimalPlaces) Returns the value of Field, rounded up to
DecimalPlaces decimal places. The parameter
DecimalPlaces is optional. (details)
RoundAccounting(Value, DecimalPlaces) Returns Value, rounded to the nearest number
with DecimalPlaces decimal places. The
parameter DecimalPlaces is optional. (details)
72

-- 72 of 261 --

Language construct with argument(s) Description
RoundAccountingValue(Field, DecimalPlaces) Returns the value of Field, rounded to the
nearest number with DecimalPlaces decimal
places. The parameter DecimalPlaces is optional.
(details)
Abs(Value) Returns the absolute value of Value. (details)
AbsValue(Field) Returns the absolute value of Field. (details)
Length(Field) Returns the number of characters of the value of
Field. (details)
Rounding
RoundDown and RoundDownValue always round in the direction of negative infinity, which means that
the value of the rounded number is never increased (see Table 21).
RoundUp and RoundUpValue always round in the direction of positive infinity, which means that the
value of the rounded number is never decreased (see Table 21).
RoundAccounting and RoundAccountingValue always round in the direction of the next number (when
indicated, with decimal places). If both neighbors are equidistant, then the (Field) value is rounded
away from zero. (see Table 21).
The optional parameter DecimalPlaces defines the number of decimal places the returned value
should have. This must be an integer between 1 and 14.
Table 21. Examples of rounding operations
-1.5 1.777 -1.777 2/3 1.5
RoundDown(Value) -2 1 -2 0 1
RoundDown(Value, 2) -1.5 1.77 -1.78 0.66 1.5
RoundUp(Value) -1 2 -1 1 2
RoundUp(Value, 2) -1.5 1.78 -1.77 0.67 1.5
RoundAccounting(Value) -2 2 -2 1 2
RoundAccounting(Value, 2) -1.5 1.78 -1.78 0.67 1.5
The Value parameter for Rounding, RoundDown, and RoundUp must be a number value. For example, this
could be the result of a calculation.
RoundUp(Sum(Products*/TotalPrice), 2) != [TotalPrice]
The language constructs RoundAccountingValue, RoundDownValue and RoundUpValue require parameter
Field to have type Number.
73

-- 73 of 261 --

RoundUpValue(TotalPrice) < 10
NOTE RoundAccounting([Field]) and RoundAccountingValue(Field) are equivalent.
Absolute values
Abs and AbsValue return the absolute amount of a value or Field value. The parameter for Abs needs
to be a number value, such as the result of a calculation.
Abs([TotalPrice]-[DeliveryCosts]) < 1
AbsValue requires the parameter to be a Field of type Number.
AbsValue(TotalPrice) < 10
NOTE Abs([Field]) and AbsValue(Field) are equivalent.
Length
Length returns the total number of characters contained in the value of Field. It can only be used for
a single Field of type String.
Generally, the length of String Fields should be limited by specifying a maximum length in the Field
definition. The operation Length makes it possible to only set restrictions on the Field length in
specific instances. Additionally, it can be used to generate a specialized error message, as opposed
to using the general error message StringTooLong.
Length(ProductNumber) != 10
The condition is satisfied if the value of ProductNumber does not consist of exactly 10
characters.
NOTE
Length of strings which contain combined characters. If the chosen character set
allows combined characters like C̨̆ , which is defined by the Unicode code point
sequence (0x0043, 0x0328, 0x0306), then the length of this combined character is
given by the length of the defining sequence. Thus, C̨̆ has length 3. For example, if
a Field F of type string has the value C̨̆e12 , then Length(F) returns 6.
NOTE
An exception exists for attachments or Fields that are not validated. In this case, the
Rule condition must be formulated as Length(Field) > Constant. Since Fields that
are not validated are not examined, this Rule would not be evaluated. This Rule is
only used to set the Field’s length to the constant in the meta interface of the
validation generator. Subsequently, this information can be used for further
74

-- 74 of 261 --

processing by any application that uses the validation generator.
4.8.2. Arithmetic language constructs for value lists
Table 22. Arithmetic language constructs for value lists
Language construct with argument(s) Description
Max(ValueList) Returns the maximum of the values in ValueList.
(details)
Min(ValueList) Returns the minimum of the values in ValueList.
(details)
MaxValue(FieldList) Returns the maximum value of the Fields in
FieldList. (details)
MinValue(FieldList) Returns the minimum value of the Fields in
FieldList. (details)
Sum(FieldList) Returns the sum of the values of the filled Fields
in FieldList. (details)
SumOfProducts(Field1, Field2) For each repetition, the product of two
repeatable Fields is calculated. The results are
summed up. (details)
SumOfTerms(Path, Term) For each repetition, the value of an iterating
term is calculated and then those values are
summed up. (details)
Maximum and Minimum
Max and Min return the maximum and minimum value of valueList, respectively. If dates are given,
Max returns the latest and Min the earliest date found in valueList. If a referenced value in the
valueList is an unspecified Field, the value 0 is used to substitute the missing value.
The valueList must adhere to the following criteria:
• it can have any number of Fields and/or calculated values
• it can have at most one constant
• the values must have type Number or Date
• all values must have the same type
MaxValue and MinValue return the maximum and minimum Field values found in FieldList,
respectively. The Fields must either have the type Number or Date, and they must have the same
type.
Unspecified Fields are not taken into consideration. If FieldList has no specified Fields and consists
solely of Number Fields, the language constructs MaxValue and MinValue return 0. If the same
conditions apply to a FieldList with Date Fields, an empty value is returned. A comparison
containing an empty value never returns an error.
75

-- 75 of 261 --

NOTE For Fields of type Number, maximum and minimum values can also be specified as
part of the Field definition.
NOTE
Min([Field1], [Field2], [Field3]) and MinValue(Field1, Field2, Field3) are
equivalent when the parameters are Date Fields.
Min([Field1], [Field2], [Field3]) and MinValue(Field1, Field2, Field3) are NOT
equivalent when the parameters are Number Fields.
Example: Field1 = 1, Field2 = 2, Field3 is not specified
→ MinValue(Field1, Field2, Field3)= 1
→ Min([Field1], [Field2], [Field3])= 0
The same applies to Max and MaxValue.
Sum
Sum returns the sum of all specified Field values in FieldList. Only Number Fields can be used as
arguments. If a Field is not specified, then it is assigned the value 0.
Sum(Order/Products*/TotalPrice) > 100000
SumOfProducts
SumOfProducts calculates the product of each iteration for two repeatable Fields and subsequently
returns the sum of products (over all iterations). If a Field is unspecified, it is assigned the value 0.
The parameters used in SumOfProducts must adhere to the following:
• exactly two Fields (no Groups) of type Number are used
• the given Fields are found in the same Group and have at least one repeatable layer
• an asterisk notates only the Fields' lowest repeatable layer
SumOfProducts(Order/Products*/UnitPrice, Order/Products*/Amount) > 100000
The product of UnitPrice and Amount is calculated for each iteration. Afterward, the sum of all
products is calculated. This value corresponds to the TotalPrice in Group Order.
SumOfTerms
The language construct SumOfTerms(Path, Term) has two arguments:
• Path is the path of a group, and some groups in the path notation have stars ('path with stars')
• Path may be extended to Path Having Filtercondition (cf. Option to specify a filter condition)
• Term is a numerical term
What is returned is the sum of the evaluations of the term. The stars in the path specify, over which
76

-- 76 of 261 --

repeatable group(s) the sum is taken.
Example 1 In the Model
the Rule Condition
Abs(SumOfTerms(Shares*,[Share_Numerator]/[Share_Denominator])-1) > 0.01
expresses that the sum of the quotients [Share_Numerator]/[Share_Denominator] over all filled
instances of the Group Shares differs from 1 (with toleranz 0,01).
For the Document
Shares Share_Numerator Share_Denominator
[1] 1 4
[2] 1 4
[3] 1 10
[4] 1 10
[5] 5
[6] 5 0
[7] 5
SumOfTerms(Shares*,[Share_Numerator]/[Share_Denominator]) returns the value 1/4 + 1/4 + 1/10 + 1/10
+ 0/5 = 0.7, thus the rule fires.
• In the 5th repetition of Shares, for the non-specified field Share_Numerator the value 0 is used
(as always for non specified number fields).
• In the repetitions 6 and 7 of Shares, the quotient is not defined (division by zero). Such
repetitions are ignored when summing up.
77

-- 77 of 261 --

Conditions that must be met
• For the argument Path:
◦ Path is the absolute or relative path of a Group.
◦ The lowest (last) element of Path is a repeatable Group with an asterisk.
◦ Path may contain more than one star; however, if one Group G has an asterisk, all of the
repeatable Groups of the path below G must have an asterisk as well.
• For the argument Term:
◦ Term is a numerical term.
◦ Term iterates over the lowest element of Path.
◦ Term iterates at most over repeatable groups which occur in Path (as an absolute path).
◦ Term does not contain a Filter Operator
◦ Term does not contain SumOfTerms.
◦ Term does not invoke parallel iteration.
◦ Term does not contain a semantic index.
If Path contains more than one asterisk, the sum is taken over all of the groups of Path which have
an asterisk.
Meaning of the asterisks in Path
Example 2 For the Model
and the Document
ProductClass Class Orders Amount UNitPrice
[1] A [1] 1 10
[2] 2 30
78

-- 78 of 261 --

ProductClass Class Orders Amount UNitPrice
[2] B [1] 2 10
[2] 3 20
the expression
SumOfTerms(
Produktklasse/Orders*,
[Produktklasse/Orders/Amount]*[Produktklasse/Orders/UNitPrice]
)
returns the total price (Sum of : Amount times UNitPrice) for each product class:
• for Class A: 1 * 10 + 2 * 30 = 70
• for Class B: 2 * 10 + 3 * 20 = 80
On the other hand,
SumOfTerms(
Produktklasse*/Orders*,
[Produktklasse/Orders/Amount]*[Produktklasse/Orders/UNitPrice]
)
returns the total price over all product classes
1 * 10 + 2 * 30 + 2 * 10 + 3 * 20 = 150
Option to specify a filter condition
Using Path Having Filtercondition (instead of Path) specifies over which of the repetitions the sum
is taken.
Example 3 In the Model
79

-- 79 of 261 --

the expression
SumOfTerms(Shares* Mit [Shares/Class]=="A",
[Shares/Shares_Zaehler]/[Shares/Shares_Nenner])
returns the sum of the quotients [Share_Numerator]/[Share_Denominator] over those repetitions of
the Group Shares where Class has the value "A".
The following must hold:
Filtercondition
• is a valid condition
• iterates over at least one Group which has an asterisk in Path
• does not contain
◦ parallel iteration
◦ a semantic index
◦ again a filter operator
◦ again the language construct SumOfTerms
◦ RepetitionNotUnique
◦ CustomCondition
4.9. Date and Time language constructs
There are three types of date specifications:
• Date: date without the time
• Time: time without the date
• DateTime: a combination of date and time
80

-- 80 of 261 --

The Date Field type can also be specified using the following date fragments:
• only month,
• only day and month,
• only year and month,
• only year.
Time specifications can be given using hours, minutes, and seconds. DateTime specifications can
consist of the year, month, day, hour, minutes and seconds.
Furthermore, the DateRange type, an interval constrained by two Date values, is also supported.
Different formats exist for this type.
4.9.1. Date and time calculations
NOTE
Using the Base Year In Models that define a Base Year, the Base Year is set as the
year for date fragments that do not specify a year. However, if the Model does not
define a Base Year in this case, a language construct requiring a year specification
may not be used.
Table 23 briefly describes the language constructs for date and time calculations.
Table 23. Date and time calculations
Language construct with argument(s) Description
AddYears(Date, Integer) Returns Date plus Integer years. Integer may be
negative. details
AddMonths(Date, Integer) Returns Date plus Integer months. Integer may
be negative. details
AddDays(X, Integer) Returns X plus Integer days. X can be Date or
DateTime. Integer may be negative. details
AddHours(DateTime, Integer) Returns Date plus Integer hours. Integer may be
negative. details
AddMinutes(DateTime, Integer) Returns Date plus Integer minutes. Integer may
be negative. details
AddSeconds(DateTime, Integer) Returns Date plus Integer seconds. Integer may
be negative. details
DifferenceInYears(Date1, Date2) Returns the difference of the two date values in
years. details
DifferenceInMonths(Date1, Date2) Returns the difference of the two date values in
months. details
DifferenceInDays(Date1, Date2) Returns the difference of the two date values in
days. details
81

-- 81 of 261 --

Language construct with argument(s) Description
DifferenceInHours(DateTime1, DateTime2) Returns the difference of the two date values in
hours. details
DifferenceInMinutes(DateTime1, DateTime2) Returns the difference of the two date values in
minutes. details
DifferenceInSeconds(DateTime1, DateTime2) Returns the difference of the two date values in
seconds. details
DayFromDate(X) X can be Date or DateTime. Returns the day of
the date value as a number. details
MonthFromDate(X) X can be Date or DateTime. Returns the month of
the date value as a number. details
YearFromDate(X) X can be Date or DateTime. Returns the year of
the date value as a number. details
QuarterFromDate(X) X can be Date or DateTime. Returns the year of
the date value as a number. Returns the quarter
of the date value as a number between 1 and 4.
details
HoursFromTime(X) X can be Time or DateTime. Returns the hours of
the date value as a number. details
MinutesFromTime(X) X can be Time or DateTime. Returns the minutes
of the date value as a number. details
SecondsFromTime(X) X can be Time or DateTime. Returns the seconds
of the date value as a number. details
DateFromDateTime(DateTime) Returns the date of DateTime. details
TimeFromDateTime(DateTime) Returns the time of DateTime. details
The date calculations StartOfDateRange and EndOfDateRange are described in Section Date Ranges.
AddYears
AddYears returns a date that has been incremented or decremented (for negative values) by the
given number of years.
NOTE
The following applies to AddYears and its corresponding language constructs
AddMonths, AddDays, AddHours,AddMinutes, and AddSeconds:
• If AddYears does not specify a date Field, an invalid date is generated.
• Comparisons that include an invalid date evaluate to false, i.e. they never result
in an error.
• A Field reference cannot have an asterisk.
• If AddYears is based on an unspecified Number Field, a 0 is set as the value for the
calculation, i.e. the construct returns an unchanged date.
82

-- 82 of 261 --

• A floating-point number can also be used to specify the number of years.
However, this value is reduced (without rounding) to its integer value.
Examples using AddYears:
date1 = 10.10.2010
AddYears(date1, 2) returns 10.10.2012
AddYears(date1, -2) returns 10.10.2008
Examples using a leap year:
date1 = 28.02.2010
AddYears(date1, 2) returns 29.02.2012
date1 = 28.02.2012
AddYears(date1, 2) returns 28.02.2014
AddYears(date1, 4) returns 28.02.2016
date1 = 29.02.2012
AddYears(date1, 2) returns 28.02.2014
AddYears(date1, 4) returns 29.02.2016
Additionally, different constructs can be combined, for example:
AddYears(date3, DifferenceInYears(date1, date2))
or
AddYears(date3, DifferenceInYears(date1, Today))
AddMonths
AddMonths returns a date that has been incremented or decremented (for negative values) by the
given number of months.
Please take the following note into consideration.
83

-- 83 of 261 --

Examples using AddMonths:
AddMonths("30.01.2009", 1) returns 28.02.2009
because there is no 30th day in February, the last day of the month is used. In other words, the
day is set to the 28th because 2009 is not a leap year.
AddMonths("30.01.2009", 2) returns 30.03.2009
March 30th is used because it is a valid day.
AddMonths("31.01.2009", 3) returns 30.04.2009
because there is no 31st day in April, the last day of the month is used.
AddMonths("30.04.2009", 1) returns 30.05.2009
The 30th of May is a valid date. Therefore, the result uses the 30th, even if it’s the last day of
the month of the first date and not the last day of the month of the second date.
AddMonths("28.02.2011", 12) returns 28.02.2012
The result uses the 28th, even if it’s the last day of the month of the first date and not the last
day of the month of the second date.
AddMonths("28.02.2012", 12) returns 28.02.2013
AddMonths("29.02.2012", 12) returns 28.02.2013
As seen in the examples, if the day specified in the first date is valid in the resulting month, it is
used in the final calculated date. If it is not valid, the last day of the given month is used.
Different constructs can be combined. For example:
AddMonths(date3, DifferenceInMonths(date1, date2))
or
AddMonths(date3, DifferenceInMonths(date1, Today))
84

-- 84 of 261 --

AddDays
AddDays returns a date that has been incremented or decremented (for negative values) by the given
number of days.
Please take the following note into consideration.
Examples using AddDays:
AddDays("10.01.2009", 1) returns 11.01.2009
AddDays("30.01.2009", 3) returns 02.02.2009
AddDays("01.01.2009",31) returns 01.02.2009
AddDays("28.02.2010", 2) returns 02.03.2010
AddDays("28.02.2012", 2) returns 01.03.2012 , since 2012 was a leap year.
AddDays(DateTime("30.01.2009", "12:00:00"), 1) returns [31.01.2009, 12:00:00]
AddDays(DateTime("28.02.2012", "12:00:00"), 2) returns [01.03.2012, 12:00:00]
The times given in the DateTime constructs are adopted for the rest of the calculation. Different
constructs can be combined.
AddDays(dateTime3, DifferenceInDays(dateTime1,dateTime2))
AddDays(dateTime3, DifferenceInDays(dateTime1, Now))
AddHours
AddHours returns a date that has been incremented or decremented (for negative values) by the
given number of hours.
Please take the following note into consideration.
Examples using AddHours:
AddHours(DateTime("30.01.2009", "12:00:00"), 4) returns [30.01.2009, 16:00:00]
AddHours(DateTime("30.01.2009", "23:00:00"), 2) returns [31.01.2009, 01:00:00]
AddHours(DateTime("30.01.2009", "12:00:00", 48) returns [01.02.2009, 12:00:00]
If AddHours is needed to determine a time, a DateTime construct containing any arbitrary date can
be used instead:
85

-- 85 of 261 --

TimeFromDateTime(AddHours(DateTime(date1, "23:00:00"), 2)) returns 01:00:00
AddMinutes
AddMinutes returns a date that has been incremented or decremented (for negative values) by the
given number of minutes.
Please take the following note into consideration.
Examples using AddMinutes:
AddMinutes(DateTime("30.01.2009", "12:00:00"), 15) returns [30.01.2009, 12:15:00]
AddMinutes(DateTime("30.01.2009", "23:45:00"), 30) returns [31.01.2009, 00:15:00]
AddMinutes(DateTime("28.02.2012", "23:00:00"), 120) returns [29.02.2012, 01:00:00]
If AddMinutes is needed to determine a time, a DateTime construct containing any arbitrary date can
be used instead:
TimeFromDateTime(AddMinutes(DateTime(date1, "23:00:00"), 75)) returns 00:15:00
AddSeconds
AddSeconds returns a date that has been incremented or decremented (for negative values) by the
given number of seconds. AddSeconds cannot be used with dates that do not have the seconds
specified.
Please take the following note into consideration.
Examples using AddSeconds:
AddSeconds(DateTime("30.01.2009", "12:00:00"), 15) returns [30.01.2009, 12:00:15]
AddSeconds(DateTime("30.01.2009", "23:59:30"), 90) returns [31.01.2009, 00:01:00]
If AddSeconds is needed to determine a time, a DateTime construct containing any arbitrary date
can be used instead:
TimeFromDateTime(AddSeconds(DateTime(date1, "23:59:00"), 75)) returns 00:00:15
DifferenceInYears
DifferenceInYears returns the difference between two given dates in years.
86

-- 86 of 261 --

The difference returned is the greatest number of years that can be added to the first date, which
does not result in a date that occurs later than the second date. A negative value is returned if the
first date lies after the second date.
NOTE
The following applies to DifferenceInYears and its corresponding language
constructs DifferenceInMonths, DifferenceInDays, DifferenceInHours,
DifferenceInMinutes and DifferenceInSeconds:
1. If the second date lies before the first date, a negative value is returned.
2. If the date is based on an unspecified Field, a 0 is set as the date difference. For
this reason, it is essential to verify that all given Fields are specified.
3. Fields may not contain an asterisk.
4. The behaviour of the date time language constructs DifferenceInHours,
DifferenceInMinutes and DifferenceInSeconds in connection with change
between daylight time and standard time can depend on the programming
language used in the generated code. For example, for the result of
DifferenceInSeconds/Minutes/Hours("2022-10-30T01:59:59", "2022-10-
30T02:00:00")
can vary depending on the system / programming language.
Table 24. Some examples using 'DifferenceInYears'
Date1 Date2 DifferenceInYears(Date1, Date2)
28.11.2024 28.11.2025 1
28.11.2024 07.01.2028 3
28.11.2024 27.11.2026 1
28.11.2024 29.11.2027 3
29.11.2027 28.11.2024 -3
Table 25. Some examples using 'DifferenceInYears' which involve leap years
Date1 Date2 DifferenceInYears(Date1, Date2)
28.02.2023 28.02.2024 0
28.02.2023 29.02.2024 1
This follows from the fact that AddYears("28.02.2023",1) returns 29.02.2024.
DifferenceInMonths
DifferenceInMonths returns the difference between two dates in months.
The calculated difference is the greatest number of months that can be added to the first date
specified, which does not result in a date later than the second date specified.
28.2.2012 + 12 months = 28.2.2013
87

-- 87 of 261 --

29.2.2012 + 12 months = 28.2.2013
As seen in the examples, if the day specified in the first date is valid in the resulting month, it is
used in the final calculated date. If it is not valid, the last day of the given month is used.
Please take the following note into consideration.
Examples:
date1 = 31.01.2010, date2 = 31.03.2010
DifferenceInMonths(date1, date2) returns the value 2
date1 = 31.01.2010, date2 = 30.03.2010
DifferenceInMonths(date1, date2) returns the value 1
date1 = 31.01.2010, date2 = 28.02.2010
DifferenceInMonths(date1, date2) returns the value 1
because the 31st does not exist in the month specified in the second date.
Examples using a leap year:
date1 = 28.02.2010, date2 = 28.02.2012
DifferenceInMonths(date1, date2) returns the value 24
date1 = 28.02.2012, date2 = 28.02.2013
DifferenceInMonths(date1, date2) returns the value 12
date1 = 29.02.2012, date2 = 28.02.2013
DifferenceInMonths(date1, date2) returns the value 12
DifferenceInDays
DifferenceInDays returns the difference between two dates in days. Both parameters must specify
a valid date; otherwise, this construct cannot be used.
88

-- 88 of 261 --

Please take the following note into consideration.
Examples
date1 = 10.10.2010, date2 = 12.10.2010
DifferenceInDays(date1, date2) returns the value 2
DifferenceInDays(date2, date1) returns the value -2
dateTime1 = 10.10.2010, 12:00:00, dateTime2 = 12.10.2010, 11:00:00
DifferenceInDays(dateTime1, dateTime2) returns the value 1
dateTime1 = 10.10.2010, 12:00:00, dateTime2 = 12.10.2010, 12:00:00
DifferenceInDays(dateTime1, dateTime2) returns the value 2
DifferenceInHours
DifferenceInHours returns the difference between two dates in hours.
Please take the following note into consideration.
Examples
dateTime1 = 10.10.2010, 12:00:00, dateTime2 = 10.10.2010, 18:00:00
DifferenceInHours(dateTime1, dateTime2) returns the value 6
dateTime1 = 10.10.2010, 10:00:00, dateTime2 = 11.10.2010, 12:00:00
DifferenceInHours(dateTime1, dateTime2) returns the value 26
DifferenceInHours(dateTime2, dateTime1) returns the value -26
If DifferenceInHours is needed to determine a time, two DateTime constructs with the same date can
be used instead:
DifferenceInHours(DateTime(date1, "12:00:00"), DateTime(date1, "18:00:00")) returns the
value 6
89

-- 89 of 261 --

DifferenceInMinutes
DifferenceInMinutes returns the difference between two dates in minutes.
Please take the following note into consideration.
Examples
dateTime1 = 10.10.2010, 12:00:00, dateTime2 = 10.10.2010, 12:45:00
DifferenceInMinutes(dateTime1, dateTime2) returns the value 45
dateTime1 = 10.10.2010, 23:30:00, dateTime2 = 11.10.2010, 00:45:00
DifferenceInMinutes(dateTime1, dateTime2) returns the value 75
DifferenceInMinutes(dateTime2, dateTime1) returns the value -75
If DifferenceInMinutes is needed to determine a time, two DateTime constructs with the same date
can be used instead:
DifferenceInMinutes(DateTime(date1, "12:00:00"), DateTime(date1, "11:00:00")) returns the
value -60
DifferenceInSeconds
DifferenceInSeconds returns the difference between two dates in seconds.
Please take the following note into consideration.
Examples
dateTime1 = 10.10.2010, 12:00:00, dateTime2 = 10.10.2010, 12:00:33
DifferenceInSeconds(dateTime1, dateTime2) returns the value 33
dateTime1 = 10.10.2010, 23:59:00, dateTime2 = 11.10.2010, 00:00:45
DifferenceInSeconds(dateTime1, dateTime2) returns the value 105
DifferenceInSeconds(dateTime2, dateTime1) returns the value -105
If DifferenceInSeconds is needed to determine a time, two DateTime constructs with the same date
can be used instead:
90

-- 90 of 261 --

DifferenceInSeconds(DateTime(Date1, "12:00:00"), DateTime(Date1, "12:00:33")) returns the
value 33
DayFromDate
DayFromDate returns the day of a given date as a number. DayFromDate(dateField) is regarded as
equivalent to a number constant.
NOTE
If a date is based on an unspecified Field, 0 is always returned. For this reason, it is
essential to verify that all Fields are given.
A Field reference cannot have an asterisk.
The date cannot be a string constant.
dateField = 12.03.
DayFromDate(dateField) returns the value 12
dateTimeField = 08.11.2018 15:00:00
DayFromDate(dateTimeField) returns the value 8
dateRangeField = 18.05.2021-30.07.2021
DayFromDate(EndOfDateRange(dateRangeField)) returns the value 30
MonthFromDate
MonthFromDate returns the month of a given date as a number. The date Field must specify a month.
For example, the condition MonthFromDate(DateField) == 1 is not permissible with a date Field
using the format YYYY.
MonthFromDate(dateField) is regarded as equivalent to a number constant.
Please take the following note into consideration.
Examples
dateField = 12.03.
MonthFromDate(dateField) returns the value 3
dateTimeField = 08.11.2018 15:00:00
91

-- 91 of 261 --

MonthFromDate(dateTimeField) returns the value 11
dateRangeField = 18.05.2009-30.07.2009 MonthFromDate(EndOfDateRange(dateRangeField))
returns the value 7
YearFromDate
YearFromDate returns the year of a given date as a number. YearFromDate can only be used with
dates that have a specified year.
YearFromDate(dateField) is regarded as equivalent to a number constant.
Please take the following note into consideration.
dateField = 13.09.2011
YearFromDate(dateField) returns 2011
dateTimeField = 08.11.2018 15:00:00
YearFromDate(dateTimeField) returns 2018
dateRangeField = 18.05.2020-30.07.2021
YearFromDate(EndOfDateRange(dateRangeField)) returns 2021
QuarterFromDate
QuarterFromDate returns the quarter of a given date as a number between 1 and 4. Additionally, the
specified date must adhere to the following:
• A month must be specified. In QuarterFromDate(DateField) == 1, the date Field may not use the
format YYYY.
Otherwise, QuarterFromDate(DateField) is regarded as equivalent to a number constant.
Please take the following note into consideration.
dateField = 12.05.
QuarterFromDate(dateField) returns the value 2
dateTimeField = 08.11.2018 15:00:00
QuarterFromDate(dateTimeField) returns the value 4
92

-- 92 of 261 --

dateRangeField = 18.05.2009-30.07.2010
QuarterFromDate(EndOfDateRange(dateRangeField)) returns the value 3
HoursFromTime
HoursFromTime returns the hour of a given date as a number between 00 and 23.
The specified date must adhere to the following:
• An hour must be specified.
Otherwise, HoursFromTime(timeField) is regarded as equivalent to a number constant.
Please take the following note into consideration.
timeField = 09:30:45
HoursFromTime(timeField) returns the value 9
dateTimeField = 08.11.2018 15:00:00
HoursFromTime(dateTimeField) returns the value 15
MinutesFromTime
MinutesFromTime returns the minutes of a given date as a number.
The specified date must adhere to the following:
• The minutes must be specified.
• The specified date cannot be a string constant.
Otherwise, MinutesFromTime(timeField) is regarded as equivalent to a number constant.
Please take the following note into consideration.
timeField = 09:30:45
MinutesFromTime(timeField) returns the value 30
dateTimeField = 08.11.2018 15:50:30
MinutesFromTime(dateTimeField) returns the value 50
93

-- 93 of 261 --

SecondsFromTime
SecondsFromTime returns the seconds of a given date as a number. The specified date must adhere to
the following:
• The seconds must be specified.
• The specified date cannot be a string constant.
Otherwise, SecondsFromTime(timeField) is regarded as equivalent to a number constant.
Please take the following note into consideration.
Example:
timeField = 09:30:45
SecondsFromTime(timeField) returns the value 45
dateTimeField = 08.11.2018 15:50:30
SecondsFromTime(dateTimeField) returns the value 30
DateFromDateTime
DateFromDateTime returns the date of a given DateTime operator.
The DateTime operator must adhere to the following:
• A date must be specified.
• The DateTime operator cannot be a string constant.
Otherwise, DateFromDateTime(timeField) is regarded as equivalent to a Date construct.
Please take the following note into consideration.
An invalid date is generated if DateFromDateTime is used with an unspecified Field. Comparisons that
include an invalid date evaluate to false, i.e. they never result in an error. For this reason, it is
essential to verify that all Fields are specified. A Field reference cannot have an asterisk.
Example:
dateTimeField = 08.11.2018 15:50:30
DateFromDateTime(dateTimeField) returns the date 08.11.2018
94

-- 94 of 261 --

TimeFromDateTime
TimeFromDateTime returns the time of a given DateTime operator. The DateTime operator must adhere
to the following:
• The time must be specified.
• The DateTime operator cannot be a string constant.
Otherwise, TimeFromDateTime(timeField) is regarded as equivalent to a Time construct.
An invalid time is generated if TimeFromDateTime is used with an unspecified Field. Comparisons
that include an invalid time evaluate to false, i.e. they never result in an error. For this reason, it is
essential to verify that all Fields are specified. A Field reference cannot have an asterisk.
Examples:
dateTimeField = 08.11.2018 15:50:30
TimeFromDateTime(dateTimeField) returns the time 15:50:30
Examples for Date and Time calculations
Example 1
Be aware that
AddYears("28.02.2023", 1) returns 29.02.2024
AddMonths("28.02.2023", 12) returns 28.02.2024
and thus,
DifferenceInYears("28.02.2023", "28.02.2024") returns 0
DifferenceInMonths("28.02.2023", "28.02.2024") returns 12
Example 2
Given the value of a date-Field DateOfBirth, we want to calculate the 18th birthday of a person. The
following table shows that neither AddMonths nor AddYears work without closer inspection.
DateOfBir
th
18th
birthday
AddYears(DateOfBirth,18) AddMonths(DateOfBirth,216)
28.02.2006 28.02.2024 29.02.2024 28.02.2024
29.02.2000 01.03.2018 28.02.2018 28.02.2018
95

-- 95 of 261 --

Therefore, a case distinction like
• if DateOfBirth is not 29. February, use AddMonths(DateOfBirth,216)
• if DateOfBirth is 29. February, use AddDays(AddMonths(DateOfBirth,216),1)
solves the problem.
Now we want to specify a rule which fires if the value of a date-Field DateOfBirth shows that the
person is not yet 18 years old. The natural candidate is a rule R with condition
DifferenceInYears(DateOfBirth, Today) < 18
works in most cases. Note, however, that
DateOfBirth Today DifferenceInYears(DateO
fBirth, Today)
Rule R
29.02.2004 28.02.2022 18 does not fire, even though
the person is not yet 18
years old
28.02.2006 28.02.2024 17 does fire, even though
already 18 years old
In order to solve this, we apply a case distinction 'birthday is not a 29 February' or 'birthday is a 29
February' and use AddMonths rather that DifferenceInYears.
(
(
DayFromDate(DateOfBirth) != 29 or
MonthFromDate(DateOfBirth) != 2
)
and
Today < AddMonths(DateOfBirth,216)
)
or
(
DayFromDate(DateOfBirth) == 29 and
MonthFromDate(DateOfBirth) == 2 and
Today < AddDays(AddMonths(DateOfBirth,216),1)
)
4.9.2. Date and time constructs
Table 26. Date and time constructs
96

-- 96 of 261 --

Language construct with argument(s) Description
Date(Day, Month, Century, Year) Combines the parameters and returns the
corresponding constant of type Date. The
parameter Century is optional. The parameter
Year is optional if the Base Year is specified in
the Model. details
Time(Hours, Minutes, Seconds) Combines the parameters and returns the
corresponding constant of type Time. details
DateTime(Date, Time) Combines the parameters and returns the
corresponding constant of type DateTime.
details
Today Returns the current date. details
Now Returns the current date and the current time.
details
BaseYear Returns the Base Year. For example,
YearFromDate(BaseYear) returns the value 2022.
details
Date
Date with three parameters
Date is usually used with three parameters: Date(Day, Month, Year), where
Day can be
• one of the string constants "1", "2", "3", … , "31"
• a Field of type string with
◦ a pattern like \d* which ensures that the Field value is a numeric string
◦ min. length 2
◦ max. length 2
• a Field of type number with
◦ min. value 1
◦ max. value 31
• the language construct DayFromDate with an appropriate argument
Month can be
• one of the string constants "1", "2", "3", … , "12"
• a Field of type string with
◦ a pattern like \d* which ensures that the Field value is a numeric string
◦ min. length 2
97

-- 97 of 261 --

◦ max. length 2
• a Field of type number with
◦ min. value 1
◦ max. value 12
• the language construct MonthFromDate with an appropriate argument
Year can be
• one of the string constants "1800", "1801", "1802", … , "2198", "2199"
• a Field of type string with
◦ a pattern like \d* which ensures that the Field value is a numeric string
◦ min. length 4
◦ max. length 4
• a Field of type number with
◦ min. value 0
◦ max. value 9999
• the language construct YearFromDate with an appropriate argument
Date with four parameters
Date can also be used with four parameters: Date(Day, Month, Century, YearInCentury), with
conditions for Day and Month like above.
Century can be
• one of the string constants "18", "19", "20","21"
• a Field of type string with
◦ a pattern like \d* which ensures that the Field value is a numeric string
◦ min. length 2
◦ max. length 2
• a Field of type number with
◦ min. value 0
◦ max. value 99
YearInCentury can be
• one the string constants "00", "01", "02", … , "98", "99"
• a Field of type string with
◦ a pattern like \d* which ensures that the Field value is a numeric string
◦ min. length 2
◦ max. length 2
98

-- 98 of 261 --

• a Field of type number with
◦ min. value 0
◦ max. value 99
Date with two parameters
If the Model has a Base Year, Date can also be used with two parameters: Date(Day, Month), with
conditions for Day and Month like above. In this case, the year of the resulting date is the Base Year.
Valid or invalid dates
A date constructed by Date(…) is valid if:
• all of the referenced Fields are filled
• all of the referenced dates are valid
• the result is a day which really exists (for example, the date 31.11.2000 may be constructed by
Date(…), but the resulting day does not exist, because November has only 30 days).
• the resulting day is not before 16. October 1583
Otherwise, it is called invalid.
The language constructs Valid or Invalid can be used to check the validity of a constructed date.
Examples
Given
Field Value
dayField 13
yearField 1999
centuryField 20
yearInCenturyField 01
dateField 13.09.1999
and
Today = 29.10.1999
the following dates are constructed
Construction Result
Date(dayField, "3", yearField) 13.03.1999
Date(dayField, "6", centuryField,
yearInCenturyField)
13.06.2001
99

-- 99 of 261 --

Construction Result
Date("1","1",yearField) 01.01.1999
Date("1",MonthFromDate(Today),YearFromDate(dat
eField))
01.10.1999
Date("29","2",YearFromDate(dateField)) invalid - February 1999 has only 28 days
Time
The language construct Time has three parameters. These parameters are used to specify the hours,
minutes, and seconds. Each parameter is optional. If a parameter is not specified, 00 is used instead.
Minutes can only be set if hours are also specified. Seconds can only be specified if minutes and
hours are also specified.
The following can be used as parameters:
• a String Field that matches the pattern [0-9]+ and has a length of 2. Additionally, the following
must apply:
◦ the specified hour must be between 0 and 23
◦ the specified minutes must be between 0 and 59
◦ the specified seconds must be between 0 and 59
• a Number Field. Additionally, the following applies to the maximum value:
◦ the maximum number of hours must equal 23
◦ the maximum number of minutes and seconds must equal 59
• the constructs HoursFromTime, MinutesFromTime and SecondsFromTime with corresponding time
specifications. However, each language construct can only be used with its corresponding
parameter, i.e. HoursFromTime can only be used as the hour’s parameter, MinutesFromTime can
only be used as the minute’s parameter, and SecondsFromTime can only be used as the second
parameter.
An invalid date is generated if an unspecified Field is used in a Date construct. A comparison with
an invalid date evaluates to false, i.e. it never returns an error. If the invalid date is subsequently
used in a calculation (e.g. HoursFromTime), 0 is returned as the calculated value. Therefore, it is
essential to check that the Fields are specified.
Field references may not contain an asterisk.
Examples
hoursField = "13", minutesField = 01, secondsField = 45, timeField = 12:34:56, Now = 29.10.2020
05:28:41 Time(hoursField, "30", secondsField) is equivalent to 13:30:45 o’clock
Time(hoursField, minutesField, secondsField) is equivalent to 13:01:45 o’clock
Time(HoursFromTime(timeField), MinutesFromTime(Now), "00") is equivalent to 12:28:00 o’clock
100

-- 100 of 261 --

DateTime
DateTime has two parameters.
The following can be used as parameters:
• a date Field with format YYYY-MM-DD or HH:mm: ss, respectively.
• a Date or Time Field, respectively.
• String Fields with the correct formats, i.e. DD.MM.YYYY or HH:mm:ss, respectively.
• the result of date or time calculations, such as AddYears.
An invalid date construct is generated if an unspecified Field is used in a DateTime construct. A
comparison with an invalid date-time evaluates to false, i.e. it never returns an error. If the invalid
date-time is subsequently used in a calculation (e.g. HoursFromTime or DifferenceInYears), 0 is
returned as the calculated value. Therefore, it is essential to check that the Fields are specified.
Field references may not contain an asterisk.
Examples
dateField = 27.01.2018
DateTime(dateField, Time(10,30,50)) is equivalent to 27.01.2018 10:30:50 o’clock
DateTime(Date(28,02,2018), Time(12,20,00)) is equivalent to 28.02.2018 12:20:00 o’clock
DateTime("27.01.2018", "00:30:00") is equivalent to 27.01.2018 00:30:00 o’clock
DateTime(AddYears(Date(01,01,2017), 1), "12:30:00") is equivalent to 01.01.2018 12:30:00
o’clock
Today
Today always returns the day on which the evaluation is performed. However, a specific date can
also be set for testing purposes. This can be done in the interface used to generate the validation
files. The current date can be changed to fit various testing scenarios.
The current date is always exact but does not take time into account.
Now
Now always returns the current date and time of evaluation. However, a specific time can be set for
testing purposes.
The use of Now in a comparison containing == is not recommended because the validity of the
condition would be dependent on the time (to the second) the evaluation is run. For example, the
condition
101

-- 101 of 261 --

[DateTime] == Now
should not be used. However, using Now in a comparison combined with the <= operator can be
helpful. For example, the condition
"19.01.2021T12:14:16" >= Now
can be used to evaluate a defined deadline.
NOTE Now cannot be used in calculations as this would lead to inconsistencies between the
time of analysis and validation.
BaseYear
A document model can be related to a certain Base Year. This relationship is defined within the
model settings. The language construct BaseYear returns this specified Base Year. This language
construct is only permitted if the Document Model has a Base Year specified.
Examples:
The Model has as a Base Year the year 2022.
YearFromDate(BaseYear) returns 2022,
StartOfDateRange(BaseYear) returns 1.1.2022.
4.9.3. Date fragments
Consider the following Fields
Name Type Format
F_MM DateFragment MM
F_MM_DD DateFragment MM-DD
F_YYYY DateFragment YYYY
F_YYYY_MM DateFragment YYYY-MM
F Date
First, we consider a Document Model which has no Base Year specified.
Two of each of the above Fields can be compared in terms of <, >, ==, !=, <=, >= according to the
following table.
comparable? F_MM F_MM_DD F_YYYY F_YYYY_MM F
102

-- 102 of 261 --

F_MM yes yes no no no
F_MM_DD yes yes no no no
F_YYYY no no yes yes yes
F_YYYY_MM no no yes yes yes
F no no yes yes yes
The Fields can be compared with constants according to the following table.
comparable? 19.04.2000 19.04.
F_MM no yes
F_MM_DD no yes
F_YYYY yes no
F_YYYY_MM yes no
F yes no
Thus, two date (fragment) values can be compared if
• either both formats include years
• or else none of the formats includes years.
With the document
Field Value
F_MM 11
F_MM_DD 01.11
F_YYYY 2020
F_YYYY_MM 02.2020
F 02.02.2020
a Rule with Rule condition
[F_MM] == [F_MM_DD]
does fire. The missing day of the Field F_MM is interpreted as "01".
Similarly, a Rule with Rule condition
[F_YYYY] < [F_YYYY_MM] and [F_YYYY] < [F]
also fires. Both, missing month parameters and missing day parameters are interpreted as "01".
103

-- 103 of 261 --

Therefore, a rule condition like
[F_YYYY_MM] == "19.04.2000"
can never be true.
NOTE
If a Model has a Base Year specified, all kinds of date fragments and dates can be
compared. In this case, a missing year of the date fragment is interpreted as the
Base Year. For example, if the Model has Base Year 2022 then, given the Field values
F_MM_DD = 01.09
F_YYYY_MM = 09.2022
the Rule with Condition [F_MM_DD] == [F_YYYY_MM] fires.
Language constructs like AddDays can be used with date fragments as described in this table:
allowed? AddDays AddMonths AddYears
F_MM no yes no
F_MM_DD yes yes no
F_YYYY no no yes
F_YYYY_MM no yes yes
F yes yes yes
Thus,
• Days can be added to date fragments whose format includes days
• Months can be added to date fragments whose format includes months
• Years can be added to date fragments whose format includes years.
4.9.4. Partially known dates
A 'partially known date' is a Field of type Date which have a 'date precision' specified. Such Fields
cannot be created within the Simple Model Editor. However, models which contain such Fields can
be imported into the Simple Model Editor.
Table 27. Kinds of date precision
Date precision Meaning Examples
DAY_OPTIONAL This can be a date where the
day may be unspecified. In this
case, 00 is used for the day.
24.12.2024
00.12.2024
104

-- 104 of 261 --

Date precision Meaning Examples
MONTH_OPTIONAL This can be a date where both,
the day and the month may be
unspecified. In this case, 00 is
used for the day, the month
respectively.
24.12.2024
00.12.2024
00.00.2024
YEAR_OPTIONAL This can be a date where the
day, the month, and the year
may be unspecified. In this case,
00 is used for the unspecified
parts.
24.12.2024
00.12.2024
00.00.2024
00.00.0000
For a partially known date,
• if a day is specified, a month must be specified as well;
• if a month is specified, a year must be specified as well.
The following restrictions apply to partially known dates regarding the use of date language
constructs.
• DayFromDate cannot be used with partially known dates.
• If MonthFromDate is used with a partially known date, then the date precision must be
DayOptional.
• If YearFromDate is used with a partially known date, then the date precision must be DayOptional
or MonthOptional.
• If QuarterFromDate is used with a partially known date, then the date precision must be
DayOptional.
Through the use of the language construct ValueAsDate, a partially known date can be converted to
a concrete date.
ValueAsDate
ValueAsDate(Field, DateSpecification) returns the value of Field as a date. The Field must be a
partially known date. Either FirstDay or LastDay are considered valid 'date specifications'. The
'date specification' indicates if a partially known date should be replaced with the earliest or latest
possible date. If 00.00.0000 is given as the Field’s value, then the Field value is treated as an invalid
date.
Table 28. Examples using ValueAsDate
Value of F ValueAsDate(F, FirstDay) ValueAsDate(F, LastDay)
00.02.2024 01.02.2024 29.02.2024
12.02.2024 12.02.2024 12.02.2024
00.00.2024 01.01.2024 31.12.2024
105

-- 105 of 261 --

4.9.5. Date Ranges
Date Ranges describe date intervals which are given by a start date and an end date. The following
formats are available:
• YYYY/YYYY
• YYYY-MM-DD/YYYY-MM-DD
• MM/MM
• YYYY-MM/YYYY-MM
• MM-DD/MM-DD
Start- and end dates can be extracted with the language constructs StartOfDateRange and
EndOfDateRange. With DateRange, a Date Range can be constructed.
Table 29. Date range calculations
Language construct with argument(s) Description
DateRange(Date1, Date2) Returns the Date Range which starts with Date1
and ends with Date2. details
StartOfDateRange(DateRange) Returns the start date of the date range. details
EndOfDateRange(DateRange) Returns the end date of the date range. details
DateRange
In order to use DateRange(Date1, Date2), the following must hold true:
• either both arguments are Fields of type Date
• or else both arguments are Fields of Type Date Fragment with the same format.
The format of the constructed Date Range is as follows:
Type (and Format) of
Date1
Type (and Format) of
Date2
Format of DateRange(Date1, Date2)
Date Date YYYY-MM-DD/YYYY-MM-DD
DateFragment MM DateFragment MM MM/MM
DateFragment MM-DD DateFragment MM-DD MM-DD/MM-DD
DateFragment YYYY DateFragment YYYY YYYY/YYYY
DateFragment YYYY-
MM
DateFragment YYYY-
MM
YYYY-MM/YYYY-MM
Example:
Date1 = "12.04.2009"; Date2 = "30.05.2010"
106

-- 106 of 261 --

DateRange(Date1, Date2) returns the value 12.04.2009-30.05.2010
The language construct DateRange(Date1, Date2) cannot be used as an argument for other language
constructs. For example, a nested use like
DateRangesOverlap(DateRange(Date1, Date2), DateRange(Date3, Date4))
is not permitted. Instead, the start and end dates can be compared separately.
StartOfDateRange
StartOfDateRange returns the start date of a specified date range. A date range must be specified as
• either a DateRange Field
• or in terms of the language construct BaseYear.
The following must hold true:
• If a Field does not have a specified year and the model has a Base Year specified, then the Base
Year is used instead.
• If a Field does not have a specified month, "1" (or January) is set as the start date instead.
• If a Field does not have a specified day, "1" is set as the start date instead.
Examples:
dateRangeField = "12.04.2009-30.05.2010"
StartOfDateRange(dateRangeField) returns the value 12.4.2009
dateRangeField = "2009/2010"
StartOfDateRange(dateRangeField) returns the value 1.1.2009
StartOfDateRange(BaseYear) returns the date 1.1. of the Base Year
EndOfDateRange
EndOfDateRange returns the end date of a specified date range. A date range must be specified as
either a DateRange Field or an BaseYear constant.
The following must hold true:
• If a Field does not have a specified year and the model has a Base Year specified, then the Base
Year is used instead.
107

-- 107 of 261 --

• if a Field does not have a specified month, "12" (for December) is set as the end date instead.
• if a Field does not have a specified day, the last day of the month is set as the end date instead.
Examples:
dateRangeField = "12.04.2009-30.05.2010"
EndOfDateRange(dateRangeField) returns the value 30.5.2010
dateRangeField = "2020/2021"
EndOfDateRange(dateRangeField) returns the value 31.12.2021
EndOfDateRange(BaseYear) returns the date 31.12. of the Base Year
For
Name Type Format
F Date Fragment YYYY-MM
G Date Range YYYY-MM/YYYY-MM
a Rule with Condition
[F] == EndOfDateRange(G)
will never fire. This is because
• the missing day value of F is interpreted as the first day of the month whereas, on the other
hand
• EndOfDateRange returns the last day of the Date Range argument, which is the last day of the
month.
The following language constructs check whether Date Ranges overlap.
Table 30. Date range language constructs
Language constructs with argument(s) Description
DateRangesOverlap(FieldList) The date ranges specified by FieldList overlap.
details
AtLeastOneDateRangeOverlaps(Field IN
FieldList)
The date range of Field overlaps with the date
range of at least one Field in FieldList. details
108

-- 108 of 261 --

DateRangesOverlap
DateRangesOverlap returns true if at least two DateRange Fields in the Field list overlap. The start
and end dates specified in the date ranges are included in the evaluated interval. For example, an
overlap also exists if one date range is wholly contained in another.
Unspecified values are ignored. This ensures that an error does not occur if no values have been
specified for the listed Fields. Only validated DateRange Fields can be used in the Field list. Each of
these Fields may use different format types. For example, the date format YYYY/YYYY and
DD.MM.YYYY-DD.MM.YYYY both specify valid date ranges. However, if a date range is specified
without a given date, month or year, the following applies:
• if the day is missing, the first day of the month is used for the first date and the last day for the
second date
• if the month is missing, January is used for the first date and December for the second
• an unspecified year is only permitted if the Model specifies a Base Year. In this case, the
unspecified year is substituted by the Base Year.
Example:
DateRangesOverlap(PeriodsAbroad*/DateSpecification)
The Rule condition is satisfied for the values:
PeriodsAbroad[1]/DateSpecification = "15.03.2021-31.03.2021"
PeriodsAbroad[2]/DateSpecification = "01.04.2021-01.05.2021"
PeriodsAbroad[3]/DateSpecification = "01.05.2021-06.05.2021"
because the second and third iterations overlap. 01.05.2021 is included in both date ranges.
AtLeastOneDateRangeOverlaps
AtLeastOneDateRangeOverlaps returns true if the date range for the first Field overlaps with at least
one other Field in the Field list. The start and end dates are included in the evaluated interval. An
overlap also exists if, for example, one date range is wholly contained in the other.
Unspecified values are ignored. This ensures that an error does not occur if the listed Fields have
unspecified values. The date formats for the various date ranges can be interpreted equivalently to
those for the DateRangesOverlap language construct.
Comparing Date Ranges
Two Date Range values can be compared in terms of == and !=, provided that they have the same
format. For example, conditions like
[dateRangeField1] != [dateRangeField2]
109

-- 109 of 261 --

and
[dateRangeField] != DateRange(dateField1, dateField2)
are possible. Comparing with <, >, <=, and >= is not permitted for Date Range values.
4.10. Miscellaneous
Table 31. Other validations
Language construct with argument(s) Condition satisfied
StringField PatternMatched RegExp The value of StringField matches the regular
expression RegExp. (details)
StringField PatternViolated RegExp The value of StringField violates the regular
expression RegExp.(details)
Valid(Field, CustomType) If the parameter CustomType is specified, the
evaluation checks if the value of Field is valid in
relation to the CustomType. Otherwise, the
evaluation ensures that Field is a valid
date.(details)
Invalid(Field, CustomType) If the parameter CustomType is specified, the
evaluation checks if Field has an invalid value in
relation to the CustomType. Otherwise, the
evaluation checks if Field contains an invalid
date. (details)
4.10.1. PatternMatched and PatternViolated
PatternMatched and PatternViolated are used to verify whether the value of a Field of type string
matches a certain pattern. PatternMatched and PatternViolated have two arguments, the first one is
a Field of type String, the second one is a String which expresses a pattern. The comparison
PatternMatched is true if the left side matches the regular expression. The comparison
PatternViolated is true if the left side does not match the regular expression.
The syntax used to formulate regular expressions is based on the Perl syntax. (Perl Compatible
Regular Expressions (PCRE)).
NOTE
For technical experts:
The validation uses the following:
- in Java: the standard library java.util.regex
- in Java-Script: RegExp
To ensure that regular expressions are processed homogeneously, only those that can be
interpreted the same way in all three target languages are used. However, this does not pose any
significant limitations during practical use.
110

-- 110 of 261 --

It is necessary to add a verification ensuring all Fields are specified in order to avoid unexpected
results. Otherwise, an empty string value might result in an error.
Regular expressions are used to formulate patterns using the Java syntax for regular expressions.
The following applies when describing sequences of letters and/or numbers:
• The specification is numeric and includes all numbers: [0-9]
• The specification includes only one number, e.g. 0: [0]
• The specification includes all letters, uppercase and lowercase: [A-Za-z]
• The specification can be numeric or alphanumeric: [A-Za-z0-9]
Example 1
FieldFilled(AllowanceNumber) And
[AllowanceNumber] PatternViolated "[A-Z]{8}[0-9]{2}[A-L][0-9]{2}[A-Z]"
This example evaluates whether a given allowance number has the correct structure.
A regular expression’s length (expected number of characters) can be indicated by placing a
number in curly brackets after each defined part. However, the pattern will always evaluate the
entire value and not search for partial expressions.
Table 32. Permitted elements in a pattern
Character Example Condition
Concatenation ab First a then b
Alternative ab|cd Either ab or cd
Grouping (a | b ){5} The round brackets are used to
Group elements, and the curly
brackets specify how many
times a or b may occur
Character selection [ab] Shorthand notation for a or b
Character selection [a-d] Shorthand notation for [abcd]
Character selection [a-dA-D] Shorthand notation for
[abcdABCD]
Character selection [^ace] Shorthand notation for: all
elements except a, c and e
Character selection \d Shorthand notation for [0-9]
Character selection \D Shorthand notation for [^0-9]
Character selection . Any element (not a line break)
*-quantifier a* a matches zero or more times
+-quantifier a+ a matches one time or more
111

-- 111 of 261 --

Character Example Condition
?-quantifier a? a matches either once or never
Number quantifier a{1,3} a matches one to three times
Number quantifier a{2,} a matches two times or more
Number quantifier a{5} a matches exactly 5 times
\ \( Is needed when a character has
special meaning but should still
be used as a character in the
pattern. Must be used with all
metacharacters, e.g. \( for a
bracket, or \. for a period,
otherwise it would be
interpreted as a random
symbol.
\u0022 \u0022 Is interpreted as double quotes
("). A double quote character
cannot otherwise be used in a
string constant.
Line break \n Is interpreted as 'line break' (for
Fields with line breaks
permitted).
Example 2
For a Field (with line breaks permitted) Address the pattern
.*\n?.*\n?.*\n?.*
or a rule with condition
[Address] PatternViolated ".*\n?.*\n?.*\n?.*"
assures that the text entered has at most 4 lines. This is possible, since . stands for an arbitrary
character, but not for a line break.
Example 3
For a Field (with line breaks permitted) Address the rule condition
[Address] PatternMatched "[\d\D]*Berlin[\d\D]*"
says that Address contains the word Berlin. The meaning of [\d\D]* is: 'an arbitrary combination of
characters and line breaks'.
112

-- 112 of 261 --

NOTE
In order to express 'either a character or a line break',
• [\d\D] should be used and
• (.|\n) should not be used,
because the latter can cause a stack overflow.
This is because the verification of whether a string matches a pattern is based on a
recursive search mechanism. A badly crafted pattern like
• (a+)+ or
• (.|\n)
can lead to too many paths that have to be checked. As a consequence, the system
may slow down or even collapse.
This can also be a security issue. A web search for ReDoS (Regular Expression
Denial of Service) provides further information, including sites where patterns can
be checked for safety.
NOTE
Strings which contain combined characters. If the chosen character set allows
combined characters like C̨̆ , which is defined by the Unicode code point sequence
(0x0043, 0x0328, 0x0306), then the length of this combined character is given by the
length of the defining sequence. Thus, C̨̆ has length 3.
Example
If a Field F of type String has value C̨̆e12 , then the condition
[F] PatternMatched "C.{5}"
is true. The condition states that the value of F begins with C and has a total length
of 6. Since C̨̆e12 is interpreted as
the condition is true.
4.10.2. Valid and Invalid
Valid and Invalid can be used with one or two parameters:
• Valid(SubmissionDate) evaluates the validity of a constructed date - see Date for the description
of how to construct a date
• Valid(AllowanceNumber, AllNum) evaluates the validity according to a custom type.
Valid and Invalid for constructed date specifications
In section Date, you can find a description of how to construct a date using Fields that have the
appropriate type and number constants.
113

-- 113 of 261 --

Date(Day, Month, "2019")
It is possible that no valid date can be generated from the Day and Month Fields, even if all
Fields are given and correct, e.g. for the following values: Day = 30, Month = 2.
Valid and Invalid allow a constructed date to be evaluated. The following is checked:
• Valid(DateConstruct) returns true if all referenced Fields are specified, and the constructed date
is valid.
• Invalid(DateConstruct) returns true if one of the referenced Fields is not specified and the
constructed date is invalid.
NOTE
Invalid also returns true if one of the Fields in the constructed date is not specified.
AllFieldsFilled must be used to prefix this construct if an error should only be
generated when an invalid but complete date has been set.
Valid and Invalid for the evaluation of custom types
It is possible to evaluate whether the value of a String or Enumeration Field is correct, compared to
a custom Field type, using Valid and Invalid.
By using a Rule for evaluation and not, for example, explicitly assigning a custom type to a Field, it
becomes possible:
• to return an info warning instead of an error if the Field value does not match the custom type
definition
• to create specialized error messages
A parameter may not have an asterisk. The custom type must be specified and present.
Example Valid(vatidnr, "VatIdNr") returns true if the Field vatidnr is specified and is a valid
VatIdNr (i.e. is valid when evaluated as the custom type VatIdNr).
Invalid(email, "EMail") returns true if the Field email is specified and is an invalid EMail (i.e.
is invalid when evaluated as the custom type EMail).
4.11. CustomCondition
If the validation language does not provide the power to express the desired Validation Rule,
CustomCondition can be used. The corresponding evaluation must be implemented by the system
calling this condition.
Example:
FieldFilled(id) AND CustomCondition NotReverse
114

-- 114 of 261 --

A good Rule of thumb is that a custom condition should only be used if
• introducing condition has been discussed and approved and
• the condition cannot be expressed in terms of the validation language.
5. Computation Rules
A Computation Rule computes a value (the computed value, which is typically but not necessarily a
numerical value) and maps it into a Field of fitting type (the computed Field). When the document of
a Model which contains Computation Rules is validated, the Computations are executed, which
typically makes the document grow larger:
new document = old document + computed values
A Computation Rule is specified by
• the Rule Group
• the name of the Computation Rule
• the computed Field
• the Computation table
NOTE Using the predicates CustomCondition and RepetitionNotUnique is prohibited for
Computation Rules.
5.1. Computation tables
Computation tables specify the computed value. In many cases, this involves a case distinction. In
general, Computation tables consist of three columns:
Common Precondition Precondition Calculation
<CommonPrecondition> <Precondition_1> <Calculation_1>
<Precondition_2> <Calculation_2>
… …
<Precondition_n> <Calculation_n>
• A common precondition is a condition of the validation language which determines whether the
Computation is carried out at all. If no common precondition is specified, the first column of the
Computation table may be omitted.
• Using preconditions, a Computation can be based on a case distinction. Preconditions are also
conditions of the validation language. If the Computation table has more than one precondition,
the preconditions must exclude each other (see this example).
• The calculation is a term of the validation language.
115

-- 115 of 261 --

NOTE
If multiple preconditions with different Computation terms are true, the following
error message will be shown when the document is validated:
"error text for Computation of <Rule Name>"
This will also be shown if preconditions from two (or more) separate Computation
Rules with the same computed Field and different Computation terms are true.
Given such a table, the computed value can be determined as follows:
• If the common precondition does not hold, no value is computed. Otherwise:
• If none of the preconditions hold, no value is computed.
• If one of the preconditions holds, the computed value is given by the corresponding calculation
term.
Example 1
• the computed Field is TotalAmount (computed Fields are displayed in grey)
• the name of the Computation Rule is CalculateTotalAmount (Computation Rules are displayed in
orange, followed by an arrow and the name of the computed Field)
• the Rule Group is M
• the Computation table is
Precondition Calculation
[Price]*[NumberOfItems]
In this example, there is neither a common precondition nor a precondition. Thus, the Computation
is carried out unconditionally. With this Computation Rule, the product of Price and Amount is
calculated and mapped into TotalAmount.
Example 2
116

-- 116 of 261 --

the Computation table is
Precondition Calculation
[FirstName] + " " +[LastName]
In the picture above, the calculated value is marked in red. Again, there are no preconditions. With
this Computation Rule, FirstName and SecondName are concatenated, with a blank space in
between. This example shows that Computations are not necessarily numerical.
5.2. Preconditions
Example 3
Both the Field Amount and the Field Discount do not have any decimal places. The Computation
should operate as follows:
• if the Amount is less than 100, the Discount is 0
• if the Amount is greater than or equal to 100, the Discount is 5% of the Amount (if necessary,
rounded up)
The Computation table is
Precondition Calculation
[Amount] < 100 0
[Amount] >= 100 RoundUp([Amount] * 0.05)
117

-- 117 of 261 --

For Computation with preconditions, the following must apply:
• If a Computation table has more than one row, for each row a precondition must be specified.
• All Groups which occur repeatable in the precondition must contain the computed Field.
• The computed Field may not be referenced in any of the preconditions.
5.3. Number of decimal places
If we try to solve the requirement of Example 3 with the Computation table
Precondition Calculation
[Amount] < 100 0
[Amount] >= 100 [Amount] * 0.05
(that means without rounding operation), the rule parser returns
The maximum number of fractional digits of the left-hand side (0) and of the right-
hand side (2) are different.
The terms "left-hand-side" and "right-hand-side" stem from the conception
[ComputedField]:=ComputationTerm
NOTE The maximum number of fractional digits of the computed Field must be the same
as the maximum number of fractional digits of the calculation.
Therefore, some roundig operation like RoundUp([Amount] * 0.05) must be used.
5.4. Common Precondition
Example 4
In the Model
118

-- 118 of 261 --

with
Enumeration-Field Kind
Enumeration Values
Address
POB
the Validation Rule StreetConditionallyRequired with condition
[/A/Kind] == "Address" and FieldNotFilled(Street)
makes sure that Street is filled if for Kind the value Address has been chosen.
We now want to Model a Computation Rule which is only executed if the value of Kind is Address. If
HouseNumber is specified, the values of Street and HouseNumber are concatenated, separated by an
empty space " ". If HouseNumber is not specified, only the value of Street is used. This requirement
can be met by a Computation Rule Computation with computed Field StreetHouseNumber and
Computation table
Computation
Common Precondition Precondition Calculation
[/A/Kind] == "Address" FieldFilled(HouseNumber) [Street] + " " + [HouseNumber]
FieldNotFilled(HouseNumber) [Street]
If the computed Field allows line breaks, then a line break can be added in the calculation with the
string constant
119

-- 119 of 261 --

"
"
5.5. Repeatable computed Fields
If the computed Field is repeatable, the Computation is only carried out if there are already some
values specified for the respective repetition layer.
Example 5
The calculation of CalculateTotalAmount is only carried out for those repetitions of the Group M
where either a value for Price or else a value for NumberOfItems is given.
NOTE For non-repeatable Groups, the behaviour is different, see [Non specified Fields
used in the computation term].
5.6. Non specified Fields used in the Computation term
If a number Field which is used as an argument in some numerical Computation is not filled, the
value 0 is assumed. This can lead to situations where the computed Field is calculated even though
none of the Fields which are used in the Computation term are filled.
Example 6
120

-- 120 of 261 --

In this example, the OverallCost is calculated based on 10999 EUR Personnel_Expenses plus 12%
Social_Insurance. This can be archived by
Precondition Calculation
RoundDown([Personnel_Expenses]*{1+[Social_Insu
rance]/100})
However, in doing so, the value of OverallCost is calculated even though none of the Fields
Personnel_Expenses Social_Insurance are filled. The calculated value is then 0.
This might well be intended, for example if the value of OverallCost is used in some further
Computations. In other cases, where it might be not intended that the calculation is executed
unconditionally, it is recommended to use respective preconditions:
Precondition Calculation
AllFieldsFilled(Personnel_Expenses,Social_Insu
rance)
RoundDown([Personnel_Expenses]*{1+[Social_Insu
rance]/100})
This aspect is also relevant for non-numerical Computations. Consider Example 2. If FirstName has
the value Patti and SecondName is not filled, the value
"Patti " (the string Patti plus a space character)
is computed. Consequently, the form engine renders the error message
This can be prevented by adding the precondition
AllFieldsFilled(FirstName,SecondName)
121

-- 121 of 261 --

5.7. Calculation cycles
Example 7
The combination of the Computation Rules Comp1 and Comp2 does not make any sense. Such a
situation is called a calculation cycle.
Example 8
This example shows that cycles are not always easy to detect.
5.8. Computation Rules and Parallel Iteration
Example 9
122

-- 122 of 261 --

Precondition Calculation
FieldFilled(Category) [/V/A/Amount] + [/V/B/Amount] + [/V/C/Amount]
This example shows that Parallel Iteration works for Computation Rules the same way as it works
for Validation Rules.
5.9. Computation Rules and current repetition
The functional language construct CurrentRepetition is also very convenient when it comes to
Computation Rules. For example, it can be used to create a table of fixed size:
This can be obtained by means of a Model like
123

-- 123 of 261 --

where the Computation is given by
Precondition Calculation
GroupFilled(RuleGroup) and
CurrentRepetition(RuleGroup)==1
"Vice-President"
GroupFilled(RuleGroup) and
CurrentRepetition(RuleGroup)==2
"Director"
5.10. Computation Rules as Validation Rules
For technical reasons, each Computation rule also operates as a validation rule. For example, if the
computed Field is F and the Computation table is
Precondition Calculation
FieldFilled(G) [G]
the corresponding validation rule is
FieldFilled(F) and FieldFilled(G) and [F]!=[G]
More complex Computation rule correspond to more complex validation rules, but the basic
pattern behind is always
FieldFilled(F) and … and [F]!= …
Thus, the corresponding validation rule ensures that:
124

-- 124 of 261 --

If the computed Field is filled, its value must be as defined by the Computation.
Normally, those corresponding validation rules never fire, thus the modeler does not need to be
aware of this technical background. However, in order to avoid those rare cases where the
corresponding validation rule may fire, a few such situations are described here.
Example 10
A Computation with computed Field F of type DateTime and calculation Now leads to the following
result:
This is because
• in the first place, the Computation computes the current Date and Time
• the corresponding validation rule condition FieldFilled(F) and [F]!= Now fires, because time
has elapsed since the Computation step.
The error text error text for Computation of C is automatically generated.
WARNING As you can see from the example above, the current Now-value should not be
calculated into a Field.
Example 11
A Computation with computed Field F of type number and Computation table
Precondition Calculation
[G] <= 1 1
[G] >= 1 2
corresponds to the validation rule with condition
FieldFilled(F) and ( ([G] <= 1 and [F]!= 1) or ([G] >= 1 and [F]!= 2 ) )
which is true if G has the value 1. Therefore, the preconditions of a Computation table must exclude
each other.
Example 12
Consider the Fields
125

-- 125 of 261 --

Name Type Format
F DateFragment YYYY
G Date
We want to create a Computation Rule which computes the year of G into F. A first idea might be to
simply use the Computation [G].
With this Computation, the year of G is computed into F. However, the corresponding Validation
Rule
FieldFilled(F) and [F]!= [G]
also fires, unless both the day and the month of G is 01. This is because both the missing day
information and the missing month information of F is interpreted as 01.
CAUTION It should be avoided to directly copy the value of a precise Date Field into a less
precise Date Field.
Instead, a Computation like
Date("1","1",YearFromDate(G))
should be used.
Example 13
Consider a model with Fields Start and End of type Date and a Field Range of type Date Range
(format YYYY-MM-DD/YYYY-MM-DD). A Computation with computed Field Range and Computation
table
Precondition Calculation
DateRange(Start, End)
computes the data range given by Start and End into the Field Range. In case of the document
Field Value
Start 01.01.2010
End 01.01.2000
the result is
Field Value
Range 01.01.2010-01.01.2000
as expected. However, this value is not a valid DateRange (because the end date is before the start
126

-- 126 of 261 --

date) and a respective error message is shown. Such cases can be excluded by using
[Start] <= [End]
as a precondition. The refined Computation
Precondition Calculation
[Start] <= [End] DateRange(Start, End)
is only executed if Start is an earlier or the same date as End.
6. Language constructs in alphabetical order
1. Abs
2. AbsValue
3. AddDays
4. AddHours
5. AddMinutes
6. AddMonths
7. AddSeconds
8. AddYears
9. AllFieldsFilled
10. AllGroupsFilled
11. BaseYear
12. AtLeastOneDateRangeOverlaps
13. AtLeastOneFieldFilled
14. AtLeastOneFieldValueIncludedInValueList
15. AtLeastOneGroupFilled
16. Comments
17. CurrentRepetition
18. CustomCondition
19. Date
20. DateFromDateTime
21. DateRange
22. DateRangesOverlap
23. DateTime
24. DayFromDate
127

-- 127 of 261 --

25. DifferenceInDays
26. DifferenceInHours
27. DifferenceInMinutes
28. DifferenceInMonths
29. DifferenceInSeconds
30. DifferenceInYears
31. DiffersWithToleranceRange1
32. DiffersWithToleranceRange2
33. DiffersWithToleranceRange5
34. DiffersWithToleranceRange10
35. EndOfDateRange
36. False
37. FieldFilled
38. FieldNotFilled
39. FieldValueIncludedInValueList
40. FieldValueNotIncludedInValueList
41. FieldValuesNotUnique
42. FieldValueAsString
43. FieldValueAsNumber
44. FieldsNotCollectivelyFilled
45. FirstDay
46. FirstFilledValue
47. GroupFilled
48. GroupsNotCollectivelyFilled
49. GroupNotFilled
50. HoursFromTime
51. Invalid
52. LastDay
53. Length
54. Max
55. MaxValue
56. Min
57. MinValue
58. MinutesFromTime
59. MonthFromDate
128

-- 128 of 261 --

60. MoreThanOneFieldFilled
61. NoFieldFilled
62. NoFieldValueIncludedInValueList
63. NoGroupFilled
64. NotAllFieldsFilled
65. NotAllFieldValuesIncludedInValueList
66. NotAllGroupsFilled
67. NotExactlyOneFieldFilled
68. Now
69. NumberOfDifferentValues
70. NumberOfFilledFields
71. NumberOfFilledGroups
72. NumberOfValueInFields
73. PatternMatched
74. PatternViolated
75. QuarterFromDate
76. RepetitionNotUnique
77. RangeAsNumber
78. RangeAsString
79. RoundAccounting
80. RoundAccountingValue
81. RoundDown
82. RoundDownValue
83. RoundUp
84. RoundUpValue
85. RuleGroup
86. SecondsFromTime
87. StartOfDateRange
88. Sum
89. SumOfTerms
90. SumOfProducts
91. Time
92. TimeFromDateTime
93. Today
94. True
129

-- 129 of 261 --

95. Valid
96. ValueAsDate
97. YearFromDate
7. Appendix
7.1. Validation and formal validation
7.1.1. Introduction
There are two different sources of errors messages in A12.
1. Formal Errors
2. Errors arising from Validation Rules
Whilst the error messages displayed to the end-user look identical, it is important to understand
when validity of the data is checked so that the error messages are displayed to the end-user at the
appropriate moment. The division of errors into these two categories helps reduce the number of
errors shown to a user at any one time and therefore prevents the user from being overwhelmed.
7.1.2. Formal errors
A formal error is displayed if a value does not fit the properties of a field as specified in the field
editor. For example:
• a string is entered into a field of type number
• a string does not fit the pattern of the field definition
• the maximal number of decimal places of a number field is exceeded
You can find a list of the field properties here.
Due to the fundamental importance of the field’s data type and data type configuration, formal
errors are displayed immediately on focus out. The error message shown is typically a default error
message and cannot be changed by a modeler. In addition, formal errors in data mean that the
value of this field cannot be evaluated. This goes so far, that A12 does not evaluate if the field is
filled or not. A field with a formal error is said to be "unknown" to the Kernel until this formal error
is corrected.
NOTE
Formal errors are shown immediately on focus out.
Formal errors mean that this field cannot be evaluated in any validation rule.
NOTE
Formal Errors will cause Computation Rules that reference this data to be
interrupted. This interruption stops both further entries in the Computation Table
and Data (for example Repeatable Fields) from being evaluated.
130

-- 130 of 261 --

Required by Checkbox
The required checkbox is a commonly used property on the field editor. Although this checkbox is
not part of the data type configuration, a field which is required by checkbox and is not filled leads
to a formal error as described above. This also means that this field value is unknown until this
formal error is corrected.
NOTE
Tabbing through empty required fields will not trigger this formal errors. (As you
will see later, there is no data change and therefore no trigger).
A field which is required by checkbox and is not filled cannot be evaluated in any
validation rule.
Leading and trailing spaces
The form engine trims leading and trailing spaces. For a string field, any of the values
Tracy Smith
Tracy Smith
Tracy Smith
will be interpreted as
Tracy Smith
From the kernel point of view, leading or trailing spaces trigger a formal error. But since the form
engine does not pass through leading or trailing spaces, such an error is impossible for editable
fields.
NOTE The trimming of leading and trailing spaces is applied not only to fields of type
string, but also to fields of type number and date.
A formal error which stems from leading and trailing spaces may be displayed in the case of
computed fields. Consider a computation rule which uses the computation term
[FirstName] + " " + [LastName]
in order to concatenate two strings. If FirstName has the value Nick but LastName is empty, the
value
Nick
is computed and a formal error
131

-- 131 of 261 --

is shown.
NOTE A12 does not handle 'empty strings'. For example, if F is a field of type string, the
rule condition [F] == "" is never valid, even though the field is not filled.
7.1.3. Errors arising from validation rules
Errors arising from validation rules differ from formal errors in a number of ways. These error
messages are written by the modeler and may be displayed at different levels as error, warning or
infos. In addition, validation rules are not always triggered on focus out. This means that you can
focus out of a field which is used in a validation rule and the rule might not be triggered. The
moment, when a validation rule is evaluated depends on a range of facts including:
• the fields used in the error condition of the rule
• the operators used in the error condition of the rule
• if the fields are in a group with repeatability > 1
• the widgets used in the form
These factors ensure that the number of errors shown at any one time is reduced and the user is
not overwhelmed. For example, rules that reference multiple fields are usually only evaluated once
the user has had the opportunity to enter data in each of these fields.
Finally, errors arising from a validation rule do not prevent this field from being evaluated in other
validation rules. This leads to a slightly different user experience when using validation rules.
NOTE
Validation rule are triggered by a range of different interactions.
Multiple errors arising from validation rules may be shown on the same field.
Differences in the user experience arising from the different types of errors
Although it is possible to exactly reproduce the error conditions of formal errors using Validation
Rules, the value of a field with no formal errors but with errors arising from validation rules can
always be evaluated. This difference can be seen in the following scenario.
132

-- 132 of 261 --

Consider two string fields where the minimum length is set to "4". For one field, F1, this is done
through the Data Type Configuration on the field editor whilst the other field, F2 has a Validation
Rule with the same condition:
When can a field value be evaluated - Fields
Field F1 F2
Data Type String String
Data Type Configuration - Min Length 4 (none)
Error Condition of Validation Rule (no rule) Length(F2) < 4
Error Message of Validation Rule (none) min length 4
If we model two further validation rules, one for each field, we can then test if the field values can
be evaluated. Let’s use the operator FieldFilled:
When can a field value be evaluated - Extra Rules
Error Field F1 F2
Condition FieldFilled(F1) FieldFilled(F2)
Error Message F1 is filled F2 is filled
We can now start a test and see if the extra rules can be evaluated. The validation rules used in this
example will be triggered on focus out, so we can see the results of this test by simply pressing the
tab key to move to the next field
When can a field value be evaluated - Test
Ste
p
Action Error message
1 enter 123 into F1 (none)
2 put the focus out of F1 (for example
by pressing the TAB key)
• Only values with at least 4 characters are
allowed.
3 enter 123 into F2 (none)
2 put the focus out of F2 • min length 4
• F2 is filled
As you can see, the formal error in the value of F1 means that the rule FieldFilled(F1) cannot be
evaluated. This leads to a different user experience when compared to F2, where FieldFilled(F2) is
true even though the data is not valid. This means that the user sees both error messages arising
from the two validation rules for F2 as both can be evaluated.
NOTE
Changing the error condition of the rule for F1 to FieldFilled(F1) Or
FieldNotFilled(F1) will still return the same result in the test above. The formal
error means that the field value is unknown and even this error condition cannot
be said to be true
133

-- 133 of 261 --

Relevant fields
Consider the document model
with rule
R
Error Field F1
Condition GroupFilled(RuleGroup) and FieldsNotCollectivelyFilled(F1,F2,../F3)
and a corresponding form model where the repeatability of the group K is modelled with a
detached repeat.
Table 33. Validation tests
Example Value of F1 Value of F2 Value of F3 Leave detached detail screen Full validation
1 a b R does not fire R fires
2 a R fires R fires
3 a b 7 R does not fire R does not fire
4 a 7 R fires R fires
5 a b c R does not fire R does not fire
6 a c R fires R fires
In order to understand this behaviour, bear in mind that full or partial validation of a rule depends
on the 'list of relevant fields'.
'List of relevant fields'
134

-- 134 of 261 --

Kind of validation Which fields are relevant?
Partial validation, for example
when trying to leave a detached
repeat details screen
All fields which
- are referenced in the rule condition and
- are on the screen which is being partially validated or have the
Global flag set to true and
- don’t have a value that leads to a formal error.
Full validation All fields which
- are referenced in the rule condition and
- don’t have a value that leads to a formal error.
The rule R only fires if at least one of the relevant fields is not filled and at least one of the relevant
fields is filled. The list of relevant fields explains the results in the tests of Table 33.
A field which is not on the respective screen but should be considered in a partial validation, can be
marked as Global in the field editor. If F3 is marked as a Global field in the document model, the
outcome of example 1 in Table 33 would change: Although the field is not on the detached repeat
details screen, it is added to the list of relevant fields and R fires.
7.2. Full Validation and Partial Validation
Kernel validation has two modes:
Full Validation is the main validation mode, where all fields are checked with formal validation and
all additional validation rules are evaluated.
Partial Validation offers a preliminary quick-check, where a subset of relevant fields can be
specified, and all other fields will be ignored. Partial Validation is intended for pre-checks of
individual pages of a larger form, before the user leaves that page. It guarantees to never report a
validation error that can only be fixed by changing a field other than the set of relevant fields
(specified by the application developer), so that the user will not be confronted with an error that
they cannot fix on the current page.
NOTE
Partial Validation guarantees to never report a validation error that can only be
fixed by changing a field other than the set of relevant fields, but it does not
guarantee a complete validation of the relevant fields - some checks may be skipped,
for performance optimization, to simplify the internal implementation, or
otherwise. Only a Full Validation performs complete checks.
Some examples of checks that may currently be skipped by Partial Validation: Rules that have
references to all repetitions of a field or group (see *-operator), when only some repetitions are
marked as relevant (even if the check would fail regardless of the content of the non-relevant
repetitions, e.g. in case of NotAllFieldsFilled, FieldsNotCollectivelyFilled, …). Rules with filter
conditions.
Which checks exactly are skipped or not is effectively an implementation detail of Kernel, may
arbitrarily change in any future release (although the Kernel team will mostly limit introducing
additional skipping) and projects can not depend on those details.
135

-- 135 of 261 --

7.3. Mandatory fields have an asterisk in their label
7.3.1. Introduction
In an A12 application, some controls have a star at the right-hand side of their label.
The star indicates that the corresponding field is 'mandatory'. This star is produced automatically,
based on a method of the meta-interface of the document model. In this document, we describe
how this method (let’s call it star method) works and how
• 'mandatory'
• 'required checkbox' (only if the Parent Group is filled = no)
• 'required checkbox' (only if the Parent Group is filled = yes)
• 'star'
are related to each other.
[s17] | validation_language/en/star/s17.png
As you can see from this diagram, starred fields are always mandatory but the inverse is not always
true. In addition, it is not always technically possible to identify all mandatory fields. The "star
method" is used to highlight a defined and documented sub-set of mandatory fields so that the end-
user is well-supported. This means that in the majority of cases, a star will be shown next to a
mandatory field so that the end-user is encouraged to enter valid data into this field.
7.3.2. Required checkbox
In the Field Editor of the Document Model Editor it can be specified whether a field is required.
This property comes in two variants:
• required (Only if the Parent Group is filled = no)
• required (Only if the Parent Group is filled = yes)
Depending on this choice, a validation rule is created.
For example, in
136

-- 136 of 261 --

Document Model 6.
the following rules are created:
Required
checkbox for
field
Only if the Parent Group is filled =
no
Only if the Parent Group is filled =
yes
F1 FieldNotFilled(F1) GroupFilled(A) and FieldNotFilled(F1)
F5 GroupFilled(C) and FieldNotFilled(F5) GroupFilled(K) and FieldNotFilled(F5)
• If a field has the Required checkbox checked and Only if the Parent Group is filled = no, then it
will also have a star.
• If a field has the Required checkbox checked and Only if the Parent Group is filled = yes, then
there are three options:
◦ the field has a star
◦ the field is mandatory and has no star
◦ the field is not mandatory
All this happens in terms of the validation rules which are created when the checkbox is checked.
7.3.3. The star method for validation rules
Mandatory sets
There is a clear parallel that can be drawn between setting the required checkbox for a field, F1,
and a validation rule with the error condition FieldNotFilled(F1). The complexity increases as:
• the number of referenced fields increases.
137

-- 137 of 261 --

• partial conditions are added to validation rules and combined with logical operators.
• further rules are added to the document model.
Mandatory fields are defined as follows: A field F is mandatory if there exists a set T of fields such
that for every document the following two implications are true:
• If no field of T is filled, then the document is not valid.
• If
◦ some field of T is filled and
◦ F is not filled,
then the document is not valid.
Negative language constructs
Used as error conditions, the language constructs
• FieldNotFilled
• GroupNotFilled
• NotAllFieldsFilled
have in common that they may be valid if none of the referenced fields is filled. In such cases, the
fields are mandatory and the corresponding controls have a star.
The property "mandatory" is not effectively decidable
Not all mandatory fields will be starred as it is not always possible to efficiently determine if a field
is mandatory.
Consider a rule condition
[F1]*[F2]!=10000001 and FieldNotFilled(F3)
where F1 and F2 are number fields with minimum value 2 and are required by checkbox (Only if
the Parent Group is filled = no).
• If 10000001 is a prime number, then F3 is a mandatory field. As there is no combination of F1 and
F2 that return 10000001 then [F1]*[F2]!=10000001 is true.
• [F1]*[F2]!=10000001 and FieldNotFilled(F3) is therefore true and means that F3 is mandatory.
• Therefore, the evaluation of whether F3 is a mandatory field depends on evaluating if 10000001
is a prime number.
Tests for prime numbers rely on "brute force" approaches which cycle through all possible options
until either a valid combination of F1 and F2 is found or no combination is found and the number is
therefore a prime number. This is not efficient and as this test is necessary to decide whether the
field F3 is mandatory it is excluded from the star method. This is true for a range of different cases,
and we cannot expect that mandatory fields are always found by the star method.
138

-- 138 of 261 --

Language constructs which are considered by the star method
Due to this and other limitations, the star method only considers partial conditions which are on
the following list:
• FieldFilled
• FieldNotFilled
• NoFieldFilled
• AtLeastOneFieldFilled
• NotExactlyOneFieldFilled
• MoreThanOneFieldFilled
• NotAllFieldsFilled
• FieldsNotCollectivelyFilled
• AllFieldsFilled
• GroupFilled
• GroupNotFilled
• NoGroupFilled
• AtLeastOneGroupFilled
• NotAllGroupsFilled
• GroupsNotCollectivelyFilled
• AllGroupsFilled
NOTE
If a rule condition contains language constructs which are not included in the list,
the rule as a whole is not ignored. Only the partial conditions which are not
included in the list are ignored.
For example, in
Document Model 7.
and rule condition
NumberOfDifferentValues(B*/G)> 5 or FieldNotFilled(F)
139

-- 139 of 261 --

the mandatory field F has a star. This is because the ignored part NumberOfDifferentValues(B*/G)> 5
is connected with or. Thus, if F is not filled, then the rule fires, regardless whether the ignored part
is true or not.
Other rules that are ignored and cannot lead to starred fields
Rules whose rule condition
• use the semantic index For
• use parallel iteration
• use the filter operator Having
• reference fields from more than one root group
are ignored by the mandatory method.
CAUTION In such cases, the rule as a whole is ignored, not only some partial conditions of
the rule.
For example, in
Document Model 8.
with rule condition
FieldFilled(K/G For "X") or FieldNotFilled(F2)
the mandatory field F2 has no star.
Starred when the field list contains only non-repeatable fields or groups
Let’s look at some examples of starred non-repeatable fields based on the following model:
140

-- 140 of 261 --

Document Model 9.
Exam
ple
condition of R1 condition of R2 mandatory
fields
stars
4 FieldNotFilled(K/F1) FieldsNotCollectivelyFilled(
K)
F1, F2, F3,
F4
F1, F2, F3,
F4
5 FieldNotFilled(K/F1) or
FieldNotFilled(K/F4)
F1, F4 F1, F4
6 FieldNotFilled(K/F1) and
FieldNotFilled(K/F4)
(none) (none)
7 FieldNotFilled(K/F1) or
FieldNotFilled(/B/H1)
F1, H1 (none)
8 NoFieldFilled(K) AtLeastOneFieldFilled(K) and
NoFieldFilled(L)
G1 G1
9 NumberOfFilledFields(K)<1 AtLeastOneFieldFilled(K) and
NoFieldFilled(L)
G1 (none)
Example 4
condition of R1 condition of R2 mandatory fields stars
FieldNotFilled(K/F1) FieldsNotCollectivelyF
illed(K)
F1, F2, F3, F4 F1, F2, F3, F4
141

-- 141 of 261 --

• The document is invalid if F1 is empty (rule R1). This is equivalent to required by checkbox (Only
if the Parent Group is filled = no).
◦ F1 is mandatory.
◦ The operator, FieldNotFilled() is relevant to the star method.
◦ F1 is starred
• If any field in the set, F1, F2, F3, F4, is filled then the document is invalid if any other field in
the set of fields is empty (rule R2).
◦ F1 is mandatory due to rule R1.
◦ F2, F3, F4 are therefore mandatory due to the combination of R1 and R2.
◦ The operator, FieldsNotCollectivelyFilled() is relevant to the star method.
◦ F2, F3, F4 are also starred.
NOTE
The effect of required by checkbox (Only if the Parent Group is filled = yes) can be
seen by changing R1 to GroupFilled(K) and FieldNotFilled(K/F1). No fields are
mandatory as K must not be filled by the user.
Example 5
condition of R1 condition of R2 mandatory fields stars
FieldNotFilled(K/F1)
or
FieldNotFilled(K/F4)
F1, F4 F1, F4
142

-- 142 of 261 --

• The document is invalid if either F1 or F4 are empty (rule R1).
◦ Both F1 and F4 are mandatory.
◦ The operator, FieldNotFilled() is relevant to the star method.
◦ F1 and F4 are starred.
Example 6
condition of R1 condition of R2 mandatory fields stars
FieldNotFilled(K/F1)
and
FieldNotFilled(K/F4)
(none) (none)
143

-- 143 of 261 --

• The document is invalid if F1 and F4 are empty (rule R1).
◦ The document is not invalid if F1 is filled and F4 is not filled.
◦ The document is not invalid if F4 is filled and F1 is not filled.
◦ F1 and F4 are not mandatory.
Example 7
condition of R1 condition of R2 mandatory fields stars
FieldNotFilled(K/F1)
or
FieldNotFilled(/B/H1)
F1, H1 (none)
• The document is invalid if either F1 or H1 are empty (rule R1).
◦ Both F1 and H1 are mandatory.
◦ The operator, FieldNotFilled() is relevant to the star method.
◦ The star method ignores rules that reference fields of more than one root group.
◦ F1 and H1 are not starred.
Example 8
condition of R1 condition of R2 mandatory fields stars
NoFieldFilled(K) AtLeastOneFieldFilled(
K) and
NoFieldFilled(L)
G1 G1
144

-- 144 of 261 --

• The document is invalid if all fields in the set, F1, F2, F3, F4 are empty (rule R1).
◦ The operator, NoFieldFilled() is relevant to the star method.
• If any field in the set, F1, F2, F3, F4, is filled and G1 (the field contained in L) is not filled then
the document is invalid (rule R2).
◦ The partial condition AtLeastOneFieldFilled(K) is always met on a valid document due to
rule R1.
◦ The partial condition NoFieldFilled(L) is always met on a valid form due to the combination
of rule R1 and the partial condition AtLeastOneFieldFilled(K).
◦ As L only contains a single field NoFieldFilled(L) is equivalent to FieldNotFilled(G1) and the
document is invalid if G1 is not filled.
◦ G1 is mandatory.
◦ The operator, NoFieldFilled() is relevant to the star method.
◦ G1 is starred.
Example 9
condition of R1 condition of R2 mandatory fields stars
NumberOfFilledFields(K
)<1
AtLeastOneFieldFilled(
K) and
NoFieldFilled(L)
G1 (none)
145

-- 145 of 261 --

• The document is invalid if all fields in the set, F1, F2, F3, F4 are empty (rule R1).
◦ The operator, NumberOfFilledFields() is not considered by the star method, see Language
constructs which are considered by the star method .
◦ Rule R1 is ignored by the star method.
• If any field in the set, F1, F2, F3, F4, is filled and G1 (the field contained in L) is not filled then
the document is invalid (rule R2).
◦ The partial condition AtLeastOneFieldFilled(K) is always met on a valid document due to
rule R1.
◦ The partial condition NoFieldFilled(L) is always met on a valid form due to the combination
of rule R1 and the partial condition AtLeastOneFieldFilled(K).
◦ As L only contains a single field NoFieldFilled(L) is equivalent to FieldNotFilled(G1) and the
document is invalid if G1 is not filled.
◦ G1 is mandatory.
◦ The operator, NoFieldFilled() is relevant to the star method.
◦ G1 is not starred, as Rule R1 is ignored by the star method.
The star method for fields in repeatable groups
In
146

-- 146 of 261 --

Document Model 10.
the fields F3, F4, F5, F6 are all repeatable as G has a maximum number of repetitions > 1 (here it is
set to 999) . The end-user sees this repeatable group as a table in the form. They can add up to 999
to this table and these rows are normally added one by one.
In contrast to non-repeatable groups, the condition GroupFilled(Group) is True for empty rows of a
repeat, see here. This therefore changes the conditions, when a field is mandatory. The first check is
to see if GroupFilled(Group) == True for this repetition.
Closest repeatable group
To evaluate, if GroupFilled(Group) is true, we must identify, which is the closest repeatable group to
the field in question. As there is no repeatable group between F5, F6 and G, the closest repeatable
group for the fields F3, F4, F5, F6 is G.
This means that GroupFilled(G) == True for a repetition of G when:
• The user adds a new row in the application
◦ For example, by clicking on "Add" under the Table
• The user enters data into any of the fields F3, F4, F5, F6 of a specific repetition of G
◦ For example, by creating a computation rule that references a specific repetition
Mandatory sets for repeatable groups and fields
The definition of a mandatory set is extended when working with repeatable groups and fields.
Mandatory repeatable fields are defined as follows: A field F is mandatory if there exists a subset of
fields, T, belong to the closest repeatable group, G, such that for every document the following two
implications are true.
• If
◦ some field of G is filled and
147

-- 147 of 261 --

◦ no field of T is filled,
then the document is not valid.
• If
◦ some field of T is filled and
◦ F is not filled,
then the document is not valid.
Negative language constructs
Please note, that it is not possible to produce a star via a condition like
NotAllFieldsFilled(G/L)
because for such a rule the rule parser returns
Iteration for '/A/G' is specified, but the rule can fire for not filled repetitions of
this repetition level.
[MVK_NEG_CONDITION_IN_ITERATION]
We shall therefore only consider valid combinations of rules in the following examples.
Starred when the field list contains repeatable fields or groups
Let’s now look at some examples when repeatable fields are starred based on the model we have
just discussed:
Document Model 11.
Depending on the rule conditions, the following stars are produced:
148

-- 148 of 261 --

Example condition of R1 condition of R2 mandatory fields stars
10 GroupFilled(G) and
NotAllFieldsFilled
(G/L)
F5, F6 F5, F6
11 GroupFilled(G) and
FieldNotFilled(G/F
3)
FieldFilled(G/F3)
and
NotAllFieldsFilled
(G/L)
F3, F5, F6 F3, F5, F6
12 FieldNotFilled(F1) GroupFilled(G) and
FieldFilled(F1)
and
NotAllFieldsFilled
(G/L)
F1, F5, F6 F1
13 GroupFilled(G) and
NoFieldFilled(G/L)
GroupFilled(G/L)
and
NotAllFieldsFilled
(G/L,G/F3,F1)
F3, F5, F6 F3, F5, F6
Example 10
condition of R1 condition of R2 mandatory fields stars
GroupFilled(G) and
NotAllFieldsFilled(G/L
)
F5, F6 F5, F6
• The document is invalid if F5 or F6 are empty after the closest repeatable group, G, has been
filled (rule R1).
◦ The set of fields F5 and F6 are mandatory.
◦ The operator, NotAllFieldsFilled() is relevant to the star method.
◦ F5 and F6 and starred.
Example 11
149

-- 149 of 261 --

condition of R1 condition of R2 mandatory fields stars
GroupFilled(G) and
FieldNotFilled(G/F3)
FieldFilled(G/F3) and
NotAllFieldsFilled(G/L
)
F3, F5, F6 F3, F5, F6
• The document is invalid if F3 is empty after the closest repeatable group, G, has been filled (rule
R1).
◦ The fields F3 is mandatory.
◦ The operator, FieldNotFilled() is relevant to the star method.
◦ F3 is starred.
• If F3 in this group is filled then the document is invalid if F5 or F6 are empty (rule R2).
◦ F3 is mandatory due to rule R1.
◦ F5 and F6 are therefore mandatory due to the combination of R1 and R2.
◦ The operator, NotAllFieldsFilled() is relevant to the star method.
◦ F5 and F6 are also starred.
Example 12
condition of R1 condition of R2 mandatory fields stars
FieldNotFilled(F1) GroupFilled(G) and
FieldFilled(F1) and
NotAllFieldsFilled(G/L
)
F1, F5, F6 F1
150

-- 150 of 261 --

• The document is invalid if the non-repeatable field F1 is empty (rule R1).
◦ The fields F1 is mandatory.
◦ The operator, FieldNotFilled() is relevant to the star method.
◦ F1 is starred.
• If F1 is filled, then the document is invalid if F5 or F6 are empty in this group (rule R2).
◦ F1 is mandatory due to rule R1.
◦ F5 and F6 are therefore mandatory due to the combination of R1 and R2.
◦ The operator, NotAllFieldsFilled() is relevant to the star method.
◦ F1 does not have the same closest repeatable group as the fields F5 and F6. F1 is not
repeatable. The set of fields F1, F5, F6 is therefore not a subset of the repeatable group G.
See Mandatory sets for repeatable groups and fields.
◦ F5 and F6 are not starred.
Example 13
condition of R1 condition of R2 mandatory fields stars
GroupFilled(G) and
NoFieldFilled(G/L)
GroupFilled(G/L) and
NotAllFieldsFilled(G/L
,G/F3,F1)
F3, F5, F6 F3, F5, F6
151

-- 151 of 261 --

The fields F3, F5, F6 have stars by the mandatory set G.
• The document is invalid if F5 and F6 are empty after the closest repeatable group, G, has been
filled (rule R1).
◦ The operator, NoFieldFilled() is relevant to the star method.
• The document is invalid if the Group, L, is filled and any of F5, F6, F3 or F1 are empty (rule R2).
◦ The Group, L, must be filled after the closest repeatable group, G, has been filled due to rule
R1.
◦ F3, F5 and F6 are therefore mandatory due to the combination of R1 and R2.
◦ F1 is not mandatory as the user must not fill the group, G.
◦ F3, F5 and F6 have the same closest repeatable group, G.
◦ The operator, NotAllFieldsFilled() is relevant to the star method.
◦ F3, F5 and F6 are starred.
7.3.4. Computations
The mandatory method works independently of whether fields are computed fields or otherwise
used in computations. However, note that
• computed fields are displayed read-only and
• a star indicates that the user must enter something.
In many cases it might be better to avoid the combination of 'read-only' and 'star'.
152

-- 152 of 261 --

7.4. Modifying error conditions
7.4.1. Make a condition always become true
Consider a rule R with error condition ErrorCondition. If you replace ErrorCondition with
ErrorCondition or 1+1==1+1
then the rule always fires. This is because the additional partial condition 1+1==1+1 is always true
and it is connected to ErrorCondition with or. Bear in mind the logical meaning of or. If Eva says
I live in Paris or my name is Eva
then this is logically true, regardless where she lives.
7.4.2. Make a condition never become true
Consider a rule R with error condition ErrorCondition. If you replace ErrorCondition with
ErrorCondition and 1+1==1
then the rule never fires. This is because the additional partial condition 1+1==1 is connected to
ErrorCondition with and. Bear in mind the logical meaning of and. If Eva says
I live in Paris and my name is Sabine
then this is logically false, regardless where she lives.
7.4.3. Referencing additional fields
Consider a rule R with error condition ErrorCondition. If you want to additionally reference the
field F without altering the semantic of the rule condition, you can replace ErrorCondition with
ErrorCondition and (FieldNotFilled(F) or FieldFilled(F))
This, however, might affect the star method for validation rules. In order to avoid this, you can use
ErrorCondition or ([F] == [F] + "x")
if F is of type string and
ErrorCondition or ([F] == [F] + 1)
153

-- 153 of 261 --

if F is of type number.
8. Parser messages
8.1. Grammar
MVK_INCOMPLETE_INPUT
Example
For the Error Condition
NumberOfFilledFields(
the Rule parser returns the message
The condition is not complete (yet).
[MVK_INCOMPLETE_INPUT]
Background
The condition is not yet complete, but can be supplemented to a complete condition.
MVK_EXPECTED_TOKEN_NOT_FOUND
Example
For the Error Condition
NumberOfFilledFields
the Rule parser returns the message
Corrupt input or condition not complete (yet): Expected '('.
[MVK_EXPECTED_TOKEN_NOT_FOUND]
Background
The character '(' must appear after the name of language constructs such as NumberOfFilledFields.
MVK_UNEXPECTED_TOKEN
Example
For the Error Condition
154

-- 154 of 261 --

FieldFilled()
the Rule parser returns the message
Corrupt input or condition not complete (yet): Unexpected found ')'.
[MVK_UNEXPECTED_TOKEN]
Background
A field is expected as an argument after the open round bracket.
MVK_LEXER_STANDARD_ERROR
Example
For the Error Condition
FieldFilled(F) ?
the following error is returned:
Character '?' at position 18 is incorrect.
[MVK_LEXER_STANDARD_ERROR]
Background
This parser message is displayed if an invalid character, such as the character ?, is used in the error
condition.
8.2. Error Field and Iteration
MVK_ERROR_FIELD_NOT_IN_RULEGROUP
Example
In the model
155

-- 155 of 261 --

with Error Field F and Error Condition
AllFieldsFilled(../F,G)
the Rule parser returns the message
The error field '/A/K/F' is not in the rule group, but the condition specifies
iteration.
[MVK_ERROR_FIELD_NOT_IN_RULEGROUP]
Solution
Choose G as Error Field.
See
Iteration
MVK_NEG_CONDITION_IN_ITERATION
Example
In the model
156

-- 156 of 261 --

and the Error Condition
AtLeastOneFieldFilled(G,K/F)
the Rule parser returns the message
Iteration for '/A/K' is specified, but the rule can fire for not filled repetitions of
this repetition level.
[MVK_NEG_CONDITION_IN_ITERATION]
Background
If the field G is specified, the condition would also be fulfilled for unspecified repetitions of K. This
is not permitted.
Solution
One possible solution is to extend the condition so that it can only be valid for specified repetitions
of the group K:
GroupFilled(K) and AtLeastOneFieldFilled(G,K/F)
See
Iteration
MVK_ERROR_FIELD_NOT_REFERENCED
If the error field is not referenced in the error condition, the Rule parser returns an error of the
following type:
The error field '/A/K/F' is not referenced in the condition.
157

-- 157 of 261 --

[MVK_ERROR_FIELD_NOT_REFERENCED]
See
Error field
MVK_ERROR_FIELD_AND_ITERATION_INVALID
This message can occur for both cases
• without parallel iteration as well as
• with parallel iteration
Without parallel iteration
Example
In the model
with Error Field F1 and Error Condition
GroupFilled(RuleGroup) and [L/F2] < 1000
the following error is returned:
An iteration for 'L' is specified in the condition by entity 'L/F2'. There must be
either an iteration for the error field 'L/F1' in at least one reference or another
error field must be defined for the rule.
[MVK_ERROR_FIELD_AND_ITERATION_INVALID]
Background
If the error field is not referenced directly, it must be referenced indirectly at least once by a group
G so that there is no repeatably referenced group below G. group is below G. + This condition is
violated in the above example. The only reference of the error field F1 is made indirectly via the
group K. But below K is the repeatably referenced group L.
158

-- 158 of 261 --

Solution 1
Specify the F2 field as an error field.
Solution 2
Change the error condition.
GroupFilled(L) and [L/F2] < 1000
See
Rule condition
With parallel iteration
Example
In the model
with Error Field L/F and Error Condition
GroupFilled(RuleGroup) and [L/G] == [M/G]
the following error is returned:
An iteration for 'L' is specified in the condition by entity 'L/G'. There must be
either an iteration for the error field 'L/F' in at least one reference or another
error field must be defined for the rule.
159

-- 159 of 261 --

[MVK_ERROR_FIELD_AND_ITERATION_INVALID]
Background
If the error field is not referenced directly, it must be referenced indirectly at least once by a Group,
so that there is no other repeatably referenced group below Group.
This condition is violated in the above example. The only reference of the error field F is made
indirectly via the group K. But below K are the repeatably referenced groups L and M.
Solution 1
Specify the field G as an error field.
Solution 2
Change the error condition.
GroupFilled(L) and [L/G] == [M/G]
See
• Rule condition
• Parallel iteration
MVK_ERROR_FIELDGROUP_ITERATION4_INVALID
Example
In the model
with Error Field F and Error Condition
GroupFilled(RuleGroup) and [L/G] < 1000
the following error is returned:
The group 'L' is specified as iteration in the condition. The error field '/A/L/F' is
160

-- 160 of 261 --

not allowed to be referenced in the rootgroup.
[MVK_ERROR_FIELDGROUP_ITERATION4_INVALID]
Background
If the error field is not referenced directly, it must be referenced indirectly at least once by a group,
so that there is no repeatably referenced group below group.
This condition is violated in the above example. The only reference of the error field F is made
indirectly via the rootgroup A. But below A there is still the repeatably referenced group L.
Solution 1
Specify the field G as an error field.
Solution 2
Change the error condition.
GroupFilled(L) and [G] < 1000
See
Rule condition
8.3. Parallel Iteration
MVK_ERROR_FIELD_NO_INDEX
Example
For a model
with error Field F and Rule condition
161

-- 161 of 261 --

[K/F]==[L/G]
the Rule parser returns the message
For 'L' iteration is specified, but on the path of the error field no index is
specified. Therefore wildcard has to be specified or another error field must be
defined for the rule.
[MVK_ERROR_FIELD_NO_INDEX]
Background
The fields F and G are located in repeatable groups that do not contain each other. For a rule
condition such as [K/F]==[L/G] to be valid, parallel iteration is required. For this, an index field Ind
of the same type as the field L/Ind must be present in group K.
Solution
In group K, create a field Ind of the same type as the field L/Ind and declare it as an index field.
See
Parallel iteration
MVK_MULTIPLE_INDEX_FIELDS
Example
In the model
and the Error Condition
162

-- 162 of 261 --

[L/K/F]==[M/F]
the Rule parser returns the message
For '/A/L/K/F' there is more than one group with an index field.
[MVK_MULTIPLE_INDEX_FIELDS]
Background
There must be no more than one index field on the error field path and the paths of the groups that
are iterated over in parallel.
Solution
Delete the index field property for Person.
See
Rule condition
MVK_ERROR_FIELD_INVALID_INDEX
Example
In the model
with Error Field L/F and Error Condition
[L/F]==[M/F]
the Rule parser returns the message
For 'M' iteration is specified, but the index field on the error field path has
another name. Therefore wildcard has to be specified or another error field must be
defined for the rule.
163

-- 163 of 261 --

[MVK_ERROR_FIELD_INVALID_INDEX]
Background
The index fields used for a parallel iteration must have the same name.
Solution
Align the types of the index fields.
See
Parallel iteration
MVK_ERROR_FIELD_INVALID_INDEX_TYPE
Example
In the model
with Error Field L/F and Error Condition
[L/F]==[M/F]
the Rule parser returns the message
For 'M' iteration is specified, but the index field on the error field path has
another type. Therefore wildcard has to be specified or another error field must be
defined for the rule.
[MVK_ERROR_FIELD_INVALID_INDEX_TYPE]
164

-- 164 of 261 --

Background
The index fields used for a parallel iteration must be of the same type.
Solution
Align the types of the index fields.
See
Parallel iteration
MVK_PARALLEL_ITERATION_INVALID
Example
In the model
and the Error Condition (with parallel iteration)
NoFieldFilled(F,/B/G)
the following error is returned:
Parallel iteration is not allowed as an error can occur for repetitions without filled
fields.
[MVK_PARALLEL_ITERATION_INVALID]
Background
The condition would also be fulfilled if neither group A nor group B is specified. This is not
permitted.
165

-- 165 of 261 --

Solution
One possible solution is to extend the condition so that it can only become valid if at least one of the
groups A and B is specified.
GroupFilled(/B) and NoFieldFilled(F,/B/G)
See
Parallel iteration
MVK_SEMANTIC_INDEX_INVALID_ITER
Example
In the model
with enumeration fields
Person
Value
PersonA
PersonB
Error Condition
[K/Name for L/Person] == "Schmied"
and Error Field Name the Rule parser returns the error
Field 'L/Person' specifies an iteration that is not allowed and therefore must not be
used as semantic index.
166

-- 166 of 261 --

[MVK_SEMANTIC_INDEX_INVALID_ITER]
Background
The use of <field1> for <field2> is not permitted if there is a group L such that
• L is repeatable
• <field2> is contained directly or indirectly in L
• the index field is neither directly nor indirectly contained in L
In the above example, this constellation is given, which is why the corresponding message is
returned. For the similar, but differently structured model
the error condition
[K/Name for L/Person] == "Schmied"
is allowed.
See
Semantic Index
MVK_SEMANTIC_INDEX_BELOW_WILDCARD
Example
In the model
167

-- 167 of 261 --

with
Enumerat
ion field
'Person'
Value
PersonA
PersonB
Error Condition
AtLeastOneFieldFilled(/A*/K/Name for "PersonA")
and Error Field Name the Rule parser returns the error
Field '/A/K/Name' uses a semantic index, but in a parent group an asterisk is
specified. This is not allowed.
[MVK_SEMANTIC_INDEX_BELOW_WILDCARD]
Background
In this error condition, a semantic index is used for the repeatable group K, although the group A
above is referenced with a *. This is not permitted.
Solution
Use
AtLeastOneFieldFilled(/A*/K*/Name HAVING [K/Person] == "PersonA")
as Error Condition.
See
• Semantic Index
168

-- 168 of 261 --

• Filter Operator
8.4. Enumeration Fields and Categories
The following three parser messages are explained together.
MVK_INVALID_STRING_CONSTANT_FOR_ENUMERATION
MVK_INVALID_STRING_CONSTANT_FOR_ENUMERATION_CATEGORY
MVK_INVALID_STRING_CONSTANT_FOR_ENUMERATION_OR_CATEGORY
For this purpose, Person and Country are fields of the type enumeration with
Enumerat
ion field
'Person'
Value
PersonA
PersonB
and
Enumeration field 'Country'
with category Region
Value Category 'Region'
France Europe
China Asia
Example 1
For the Error Condition
FieldValueIncludedInValueList(Person,"PersonA","PersonC")
the Rule parser returns the error
At 'FieldValueIncludedInValueList': 'PersonC' is not valid for enumeration 'Person'.
[MVK_INVALID_STRING_CONSTANT_FOR_ENUMERATION]
Example 2
For the Error Condition
FieldValueIncludedInValueList(Country -> Region,"Asia","Afrika")
169

-- 169 of 261 --

the Rule parser returns the message
At 'FieldValueIncludedInValueList': 'Afrika' is not a valid value for category
'Region'.
[MVK_INVALID_STRING_CONSTANT_FOR_ENUMERATION_CATEGORY]
Example 3
For the Error Condition
NoFieldValueIncludedInValueList(Country → Region, Person In "Asia","PersonA")
the Rule parser returns the error
At 'NoFieldValueIncludedInValueList': 'X' is not valid in the used enumerations or
categories.
[MVK_INVALID_STRING_CONSTANT_FOR_ENUMERATION_OR_CATEGORY]
Background
When using the language constructs
• FieldValueIncludedInValueList
• FieldValueNotIncludedInValueList
• NoFieldValueIncludedInValueList
• AtLeastOneFieldValueIncludedInValueList
• NotAllFieldValuesIncludedInValueList
together with an enumeration field and a value list consisting of strings the following must apply:
Each string in the value list is either
• value of an enumeration field used without category access
• Category value of an enumeration field used with this category.
See
• Enumeration
• Predicate language constructs
MVK_NO_ENUMERATION
If the access operator for categories is applied to a field F that is not of type enumeration, e.g. by
170

-- 170 of 261 --

[F -> Continent] == "Europe"
the Rule parser returns the following message:
Field 'F' is no enumeration - the category operation may not be used.
[MVK_NO_ENUMERATION]
See
• Enumeration
• The field value operator
MVK_CATEGORY_UNKOWN
If the access operator for categories -> is applied to a field F of type enumeration with a value that
is not specified as the category name of F, e.g. by
[F -> Continent] == "Europe"
the Rule parser returns the following message:
Category 'Continent' is unknown.
[MVK_CATEGORY_UNKOWN]
See
Enumeration
MVK_CATEGORY_NOT_ALLOWED
Example
If G is a field of type Enumeration with category K, the Rule parser returns for the condition
AtLeastOneFieldFilled(F, G -> K)
the following message
At 'AtLeastOneFieldFilled': It is not allowed to reference a category.
[MVK_CATEGORY_NOT_ALLOWED]
Background
Access to categories is only permitted for the following language constructs:
171

-- 171 of 261 --

• Field value operator [ ]
• FieldValueIncludedInValueList
• FieldValueNotIncludedInValueList
• NoFieldValueIncludedInValueList
• AtLeastOneFieldValueIncludedInValueList
• NotAllFieldValuesIncludedInValueList
• NumberOfValueInFields
• FieldValueAsNumber
• FirstFilledValue
See
Example of how to use categories in a rule condition
8.5. Arithmetic
MVK_NO_STRING_OR_NUMBER_FOR_ADD
Example
If F is a field of type string, the Rule parser returns for the error condition
[F] + 4 == 7
the following message
Addition is allowed only for numbers or as concatenation operation for strings.
[MVK_NO_STRING_OR_NUMBER_FOR_ADD]
Background
Operation + is only permitted if
• both arguments are numbers (then + stands for the addition) or
• if both arguments are strings (then + stands for the string concatenation).
See
Arithmetic operations
MVK_NO_NUMBER_FOR_CALC
Example
If F is a field of type string, the Rule parser returns the following for error conditions such as
172

-- 172 of 261 --

[F] * 4 == 7
or
[F] - 4 == 7
or
[F] / 4 == 7
the following message
Calculations are allowed only with numbers.
[MVK_NO_NUMBER_FOR_CALC]
Background
The arithmetic operations
• Multiplication *
• Division \
• Addition +
• Subtraction -
are only permitted for numbers.
See
Arithmetic operations
MVK_INVALID_TYPE_FOR_COMPARISON
Example
If F is a field of type string, the Rule parser returns for the error condition
1 < [F]
the following message
'Less than' or 'greater than' comparative is only allowed to be used for numbers,
amounts and dates.
[MVK_INVALID_TYPE_FOR_COMPARISON]
173

-- 173 of 261 --

MVK_NOT_SORTABLE
Example
Are F, G and H fields of type string, the Rule parser returns for the error condition
MinValue(F,G) == [H]
the following message
At 'MinValue': Only numbers, amounts or dates are allowed to be used.
[MVK_NOT_SORTABLE]
Background
With the language constructs
• MinValue
• MaxWert
• Min
• Max
only numbers, amounts of money or dates may be used.
See
Maximum and Minimum
MVK_TOO_MANY_CONST_FOR_CALC_OP
Example
If an error condition contains a partial condition of the type
11 == 10
so the Rule parser returns the message
It is not allowed to compare two constants.
[MVK_INVALID_COMPARE_TO_CONST]
See
Arithmetic operations
174

-- 174 of 261 --

MVK_INVALID_COMPARE_DEC_PLACES
Example 1
If F and G are fields of type number, whereby a maximum of two decimal places are permitted for
field F and no decimal places are permitted for field G, the Rule parser returns for the error
condition
[F] == [G]
the following message
The maximum number of fractional digits of the left-hand side (2) and of the right-
hand side (0) are different.
[MVK_INVALID_COMPARE_DEC_PLACES]
Background
When comparing numerical values with == and !=, the maximum decimal places must match.
Solution
Instead of the condition
[F] == [G]
use
[F] <= [G] and [G] <= [F]
Instead of the condition
[F] != [G]
use
[F] < [G] or [G] < [F]
See
Comparison operations
Example 2
For Fields F, G
175

-- 175 of 261 --

• of type number,
• without decimal places,
• and Min. Value = 1
and Rule condition
[F] == 2 ^ [G]
the Rule parser returns the message
The maximum number of fractional digits of the left-hand side (0) and of the right-
hand side (?) are different.
[MVK_INVALID_COMPARE_DEC_PLACES]
Background
Since the exponent [G] can not be negative, the term 2 ^ [G] is an integer. However, this
consideration is not carried out by the Rule parser.
Solution
Use a rounding operation like
[F] == RoundAccounting(2 ^ [G])
See
• Arithmetic operations
• Rounding
MVK_TOO_MANY_DIV_FOR_CALC
Example
For fields F and G of type number and error condition
[G] / [F] / 2 > 0
the Rule parser returns the message
Only use one division at most for each calculation.
[MVK_TOO_MANY_DIV_FOR_CALC].
176

-- 176 of 261 --

Background
If several divisions are carried out in a term, brackets {} must be used.
Solution
Use brackets {} like
{ [G] / [F] } / 2 > 0
or
[G] / { [F] / 2 } > 0
See
Arithmetic operations
MVK_TOO_MANY_POW_FOR_CALC
Example
For Fields F and G of typ number (without decimal places) and rule condition
[G] ^ [F] ^ 2 > 0
the Rule parser returns the message
Power operations cannot be nested without brackets.
[MVK_TOO_MANY_POW_FOR_CALC].
Background
If a term contains several power operations, {} must be used.
Solution
Use brackets {} like
{ [G] ^ [F] } ^ 2 > 0
or
[G] ^ { [F] ^ 2 } > 0
177

-- 177 of 261 --

See
Arithmetic operations
MVK_INVALID_TYPE_FOR_TOLERANCE_OP
Example
If F is a field of type number and G is a field of type string, the rule passer returns for
[F] DiffersWithToleranceRange1 [G]
the following message
Comparative operation for tolerances is only allowed for numbers and amounts.
[MVK_INVALID_TYPE_FOR_TOLERANCE_OP]
Background
The language constructs DiffersWithToleranceRange1 etc. can only be used with fields of type
number or monetary amount.
MVK_INVALID_DIGIT_FOR_ROUNDING
Example
If F is a field of type number, the Rule parser returns for the error condition
RoundUpValue(F,15) >= 1
the following message
'15' is not valid for using the function round.
[MVK_INVALID_DIGIT_FOR_ROUNDING]
Background
If the rounding language constructs
• RoundDownValue
• RoundDown
• RoundUpValue
• RoundUp
• RoundAccountingValue
• RoundAccounting
178

-- 178 of 261 --

a second argument for the rounding accuracy is specified, then this argument must be an integer
between 0 and 14.
Solution
Select a valid value for the rounding accuracy, e.g.
RoundUpValue(F,2) >= 1
See
Rounding
MVK_BRACKET_MISSING
Example
For the Error Condition
FieldFilled(Name) and FieldNotFilled(FirstName) or FieldNotFilled(Company)
the Rule parser returns the message
If a condition contains 'and' as well as 'or', brackets must be used to clarify the
order of execution.
[MVK_BRACKET_MISSING]
Background
If both And and Or occur in an error condition, round brackets must be used to define how the
combination of And and Or is to be understood. It makes a big difference which brackets are used.
Solution 1
( FieldFilled(Name) And FieldNotFilled(FirstName)) Or FieldNotFilled(Company)
Solution 2
FieldFilled(Name) And (FieldNotFilled(FirstName) Or FieldNotFilled(Company))
See
Logical operators
179

-- 179 of 261 --

MVK_NO_CONST
Example
If F is a field of type number, the Rule parser returns for the error condition
[F] == Abs(2)
the following message
At 'Abs' It is not allowed to use constants.
[MVK_NO_CONST]
Background
With the language constructs
• Abs
• RoundDown
• RoundUp
• RoundAccounting
constants must not be used.
See
Rounding
MVK_INVALID_NUMBER_FOR_EXP
Example
For a Field F of type number with one decimal place and Error Condition
20 < 2 ^ [F]
the Rule parser returns the message
Please use a number without fractional digits as exponent.
[MVK_INVALID_NUMBER_FOR_EXP]
Background
When using the Power Operator a ^ b, the exponent b must be an integer.
180

-- 180 of 261 --

Solution
In order to make the exponent an integer, an appropriate rounding operation can be used, for
example
20 < 2 ^ RoundAccountingValue(F)
See
• Arithmetic operations
• Rounding
8.6. RepetitionNotUnique
MVK_NOT_IN_SAME_PATH
Example
In the model
(where K/P and L/P are index fields) and the error condition
RepetitionNotUnique(K/F,L/G)
the Rule parser returns the message
At 'RepetitionNotUnique' All referenced repeatable groups must be in the same path.
Corrupt: 'L/G'.
[MVK_NOT_IN_SAME_PATH]
181

-- 181 of 261 --

Background
With RepetitionNotUnique all referenced repeatable contexts must be on one path. Parallel iteration
is also not possible.
See
RepetitionNotUnique
MVK_NOT_IN_RULEGROUP
Example
In the model
with the Error Condition
RepetitionNotUnique(../F,G)
the Rule parser returns the message
At 'RepetitionNotUnique' Only fields in subgroups of RuleGroup or directly contained
in the repeatable rootgroup are allowed to be referenced. Corrupt: '../F'.
[MVK_NOT_IN_RULEGROUP]
Background
When using RepetitionNotUnique(fieldList) without the addition @From one of the following
conditions must apply:
• the rule group is a repeatable rootgroup that contains all fields of the field list directly or
• there is a repeatable group which is contained in the rule group and contains all the fields in
the field list.
In the example above, neither of these conditions is fulfilled.
182

-- 182 of 261 --

Solution
Use the addition @From instead.
RepetitionNotUnique(../F,G @From ..)
See
RepetitionNotUnique
MVK_GROUP_NOT_REPEATABLE
Example
In the model
and the Error Condition
RepetitionNotUnique(K/L/F @From K/L)
the Rule parser returns the message
The given group must be repeatable. Corrupt: K/L.
[MVK_GROUP_NOT_REPEATABLE]
Background
With @From <group> a reference group is specified from which the uniqueness is checked. This
reference group must be repeatable.
Solution
Select the repeatable group K as the reference group.
RepetitionNotUnique(K/L/F @From K)
183

-- 183 of 261 --

See
RepetitionNotUnique
MVK_REPEATABLE_GROUP_MISSING
For RepetitionNotUnique without specifying a reference group using @From, there must be at least
one repeatable group between the RuleGroup and each referenced field or the fields must lie
directly in a repeatable rootgroup.
Example 1
In the model
with the Error Condition
RepetitionNotUnique(F,G)
the Rule parser returns the message
At 'RepetitionNotUnique': Between RuleGroup and each referenced field must be at least
one repeatable group or fields have to be directly contained in the repeatable
rootgroup. Corrupt: 'F'.
[MVK_REPEATABLE_GROUP_MISSING]
Solution
Move the rule to group A.
Example 2
In the model
184

-- 184 of 261 --

with the Error Condition
RepetitionNotUnique(F,K/G)
the Rule parser also returns the following message
At 'RepetitionNotUnique': Between RuleGroup and each referenced field must be at least
one repeatable group or fields have to be directly contained in the repeatable
rootgroup. Corrupt: 'F'.
[MVK_REPEATABLE_GROUP_MISSING]
Solution
Move field G to group A.
See
RepetitionNotUnique
MVK_INVALID_COMBINATION_OF_REPETITON_NOT_UNIQUE
The language construct 'RepetitionNotUnique' must not be used multiple times per
condition.
[MVK_INVALID_COMBINATION_OF_REPETITON_NOT_UNIQUE]
See
RepetitionNotUnique
MVK_NOT_IN_GROUP
Example
In the model
185

-- 185 of 261 --

with the Error Condition
RepetitionNotUnique(K/F,K/L/G @From K/L)
the Rule parser returns the message
At 'RepetitionNotUnique': Only fields in subgroups of the given group 'K/L' are
allowed to be referenced. Corrupt: 'K/F'.
[MVK_NOT_IN_GROUP]
Solution
Instead, use the condition
RepetitionNotUnique(K/F,K/L/G @From K)
See
RepetitionNotUnique
MVK_PARALLEL_ITERATION_NOT_ALLOWED
Example
In the model
186

-- 186 of 261 --

with the Error Condition (with parallel iteration)
RepetitionNotUnique(L/F) and GroupsNotCollectivelyFilled(L,M)
the following error is returned:
At 'RepetitionNotUnique': Parallel iteration is not allowed.
[MVK_PARALLEL_ITERATION_NOT_ALLOWED]
Background
It is not permitted to use RepetitionNotUnique for a group that is iterated over in parallel in the same
error condition.
Solution
Use the language construct FieldValuesNotUnique
FieldValuesNotUnique(L*/F) and GroupsNotCollectivelyFilled(L,M)
See
• Rule condition
• Parallel iteration
187

-- 187 of 261 --

8.7. SumOfProducts
MVK_PARAMSIZE_INVALID_2
Example
For the Error Condition
SumOfProducts(K*/F) >= 100
the Rule parser returns the message
'SumOfProducts' must contain exactly two parameters.
[MVK_PARAMSIZE_INVALID_2]
See
SumOfProducts
MVK_DIFFERENT_GROUPS
Example
In the model
the Rule parser returns for the error condition
SumOfProducts(K*/F,K*/L/G) >= 100
the following message
All parameters at 'SumOfProducts' must be in the same group. Corrupt: 'K/L/G'.
[MVK_DIFFERENT_GROUPS]
188

-- 188 of 261 --

Solution 1
Move field G to group K and use the error condition
SumOfProducts(K*/F,K*/G) >= 100
Solution 2
Move field F to group L and use the error condition
SumOfProducts(K*/L/F,K*/L/G) >= 100
See
SumOfProducts
MVK_REPEATABLE_LEVEL_REQUIRED
Example
In the model
with the Error Condition
SumOfProducts(K/L/F,K/L/G) > 100
the Rule parser returns the message
At 'SumOfProducts': All fields must be repeatable at least at one layer.
[MVK_REPEATABLE_LEVEL_REQUIRED]
Solution
Use a simple product instead of SumOfProducts
189

-- 189 of 261 --

[K/L/F] * [K/L/G] > 100
See
SumOfProducts
MVK_WILDCARD_AT_LOWEST_REPEATABLE_LEVEL_REQUIRED
Example
In the model
with the Error Condition
SumOfProducts(K/L/F,K/L/G) > 100
the Rule parser returns the message
At 'SumOfProducts': The lowest repetition position must specify an unrestricted
asterisk.
[MVK_WILDCARD_AT_LOWEST_REPEATABLE_LEVEL_REQUIRED]
Solution
Mark the repeatable group K with an asterisk:
SumOfProducts(K*/L/F,K*/L/G) > 100
See
SumOfProducts
MVK_WILDCARD_ONLY_AT_LOWEST_LEVEL_ALLOWED
190

-- 190 of 261 --

Example
In the model
the Rule parser returns for the error condition
SumOfProducts(K*/L*/F,K*/L*/G)>=100
the following message
At 'SumOfProducts': Only the lowest repetition position is allowed to specify an
asterisk.
[MVK_WILDCARD_ONLY_AT_LOWEST_LEVEL_ALLOWED]
Solution
Remove the asterisk at K. The rule thus iterates over K.
SumOfProducts(K/L*/F,K/L*/G)>=100
See
SumOfProducts
MVK_ONLY_STRING_ENUM_NUMBER_CMP_DATE_ALLOWED
Example
In the model
191

-- 191 of 261 --

the Rule parser returns for the error condition
NumberOfDifferentValues(F,G)>1
the following message
At 'NumberOfDifferentValues': It is only allowed to use either string fields,
enumerations, numbers (including amounts) or date/time fields with the same
components. Incompatible: 'F'.
[MVK_ONLY_STRING_ENUM_NUMBER_CMP_DATE_ALLOWED]
Background
One of the following situations must exist when using the operator NumberOfDifferentValues:
• All fields in the field list are of type string or enumeration
• All fields in the field list are of the type number
• All fields in the field list are of type date
See
NumberOfDifferentValues
MVK_STRING_ENUM_AND_NON_STRING_ENUM
Example
In the model
the Rule parser returns for the error condition
192

-- 192 of 261 --

NumberOfDifferentValues(F,G)>1
the following message
At 'NumberOfDifferentValues': If the first parameter is a string or enumeration then
all parameters have to be of type string or enumeration. Corrupt: 'G'.
[MVK_STRING_ENUM_AND_NON_STRING_ENUM]
Background
One of the following situations must exist when using the operator NumberOfDifferentValues:
• All fields in the field list are of type string or enumeration
• All fields in the field list are of the type number
• All fields in the field list are of type date
See
NumberOfDifferentValues
MVK_NUMBER_AND_NON_NUMBER
Example
In the model
the Rule parser returns for the error condition
NumberOfDifferentValues(F,G)>1
the following message
At 'NumberOfDifferentValues': If the first parameter is a number (including amounts)
then all parameters have to be of type number (including amounts). Corrupt: 'G'.
[MVK_NUMBER_AND_NON_NUMBER]
193

-- 193 of 261 --

Background
One of the following situations must exist when using the operator NumberOfDifferentValues:
• All fields in the field list are of type string or enumeration
• All fields in the field list are of the type number
• All fields in the field list are of type date
See
NumberOfDifferentValues
MVK_INVALID_CONST
Example
For the Error Condition
NumberOfValueInFields("20.02.2022" In F,G ) > 0
the Rule parser returns the message
At 'NumberOfValueInFields': It is only allowed to use constants with type 'number',
'string', 'true' or 'false'.
[MVK_INVALID_CONST]
See
NumberOfValueInFields
8.8. Date
MVK_INVALID_DATE_OR_TIME_EXTRACT_OP
Example
In the model
194

-- 194 of 261 --

with the Error Condition
"01.05.1989" == Date(MonthFromDate(D1),DayFromDate(D2),YearFromDate(D3))
the Rule parser returns the message
At 'Date': at this position 'DayFromDate' is the only allowed extraction operator.
[MVK_INVALID_DATE_OR_TIME_EXTRACT_OP]
Background
If the language construct Date contains constructions with
• DayFromDate
• MonthFromDate
• YearFromDate
used as arguments, they must be used in the right place:
DayFromDate Position 1
MonthFromDate Position 2
YearFromDate Position 3
Accordingly, the order of the language construct Time must be
HoursFromTime Position 1
MinutesFromTime Position 2
SecondsFromTime Position 3
Solution
Arrange the arguments in the correct order:
195

-- 195 of 261 --

"01.05.1989" == Date(DayFromDate(D2),MonthFromDate(D1),YearFromDate(D3))
"11:11:12" == Time(HoursFromTime(D3),MinutesFromTime(D2),SecondsFromTime(D1))
See
• Date
• Time
MVK_WRONG_DATE_FORMAT_FOR_OP
Modell
Example 1
For the Error Condition
[DateField] == Date(DayField,"12","1929")
the Rule parser may return the following message
At 'Date': Wrong date format at 'DayField'.
[MVK_WRONG_DATE_FORMAT_FOR_OP]
Example 2
For the Error Condition
[DateField] == Date(DayField,"13","1929")
the Rule parser returns the message
At 'Date': Wrong date format at '13'.
[MVK_WRONG_DATE_FORMAT_FOR_OP]
196

-- 196 of 261 --

Background
In example 1, the message is displayed if the day field does not fulfil the limits
Min Value 1
Max Value 31
In the second example, the message is displayed because 13 is not between 1 and 12 and therefore
cannot represent a month.
Solution
In the first example for the day field, the limits must be specified
Min Value 1
Max Value 31
in the second example use one of the constants
"1", "2", ... , "12"
instead of "13".
The same applies to time constructions of the type
"11:11:12" == Time(HoursField,"59","00"))
Example 3
For the Fields
Name Type Format
F DateRange YYYY-MM-DD / YYYY-MM-DD
G Date
H DateFragment MM-DD
and the Error Condition
[F] != DateRange(G,H)
the Rule parser returns the message
At 'DateRange': Wrong date format at 'H'.
[MVK_WRONG_DATE_FORMAT_FOR_OP]
197

-- 197 of 261 --

Background
The arguments of DateRange must be either
• both of type Date or else
• both of Type DateFragment with the same format.
See
• Date
• Time
• DateRange
MVK_DATE_AND_NONDATE
This message can be sent both in connection with
• NumberOfDifferentValues as well as in connection with
• Min, Max, MinValue, MaxValue.
NumberOfDifferentValues
In the model
the Rule parser returns for the error condition
NumberOfDifferentValues(F,G)>1
the following message
At 'NumberOfDifferentValues': if the first parameter is a date than all parameters
have to be dates.
[MVK_DATE_AND_NONDATE]
Background
One of the following situations must exist when using the operator NumberOfDifferentValues:
198

-- 198 of 261 --

• All fields in the field list are of type string or enumeration
• All fields in the field list are of the type number
• All fields in the field list are of type date
See
NumberOfDifferentValues
Min, Max, MinValue, MaxValue
If F is a field of type date and G is a field of type number, the Rule parser returns for the error
condition
MinValue(F,G)>1
the following message
At 'MinValue': if the first parameter is a date than all parameters have to be dates.
[MVK_DATE_AND_NONDATE]
The same applies to the language constructs MaxValue, Min, Max.
Background
When using the language constructs Min, Max, MinValue, MaxValue one of the following situations must
be present:
• All fields in the field list are of type number
• All fields in the field list are of type date
See
Maximum and Minimum
MVK_DATEFORMATS_NOT_COMPATIBLE
If F and D are fields of type Date with
Field Format
F JJJJ-MM-TT’T’HH:mm:ss
G HH:mm:ss
the Rule parser returns for the error condition
NumberOfDifferentValues(F,G)==1
199

-- 199 of 261 --

the following message
Date formats not compatible for 'NumberOfDifferentValues': 'yyyy-MM-dd'T'HH:mm:ss' and
'HH:mm:ss'
[MVK_DATEFORMATS_NOT_COMPATIBLE]
Background
If the language constructs
• NumberOfDifferentValues
• MinValue
• MaxValue
are used with date type fields, their date formats must contain the same components (e.g.
yyyyMMdd and dd.MM.yyyy are compatible, but yyyyMMdd and MMdd are not).
See
• Date
• Time
MVK_INVALID_DATE_FOR_OP
Example
If F and D are fields of type date with
Field Format
F JJJJ-MM-TT’T’HH:mm:ss
G HH:mm:ss
the Rule parser returns for the error condition
[F] == DateTime(MinutesFromTime(G),"17:09:59")
the following message
At 'DateTime': 'MinutesFromTime' is no valid date.
[MVK_INVALID_DATE_FOR_OP]
Background
The language construct DateTime expects a date as the first argument.
200

-- 200 of 261 --

See
Date
MVK_INVALID_PARAMETER_FOR_DATE_RANGE_COMPARISON
Example
If F1, F2, F3, F4 are fields of type DateRange, the Rule parser returns for the error condition
AtLeastOneDateRangeOverlaps(F1, F2 In F3, F4)
the following message
At 'AtLeastOneDateRangeOverlaps': Invalid parameter before 'IN'. No groups, no
asterisk and only exactly one field is allowed.
[MVK_INVALID_PARAMETER_FOR_DATE_RANGE_COMPARISON]
Background
The language construct AtLeastOneDateRangeOverlaps is a special case in which the first field list may
only contain one field without an asterisk on group-level.
See
Date range conditions
MVK_PARAMSIZE_INVALIDFN
Example
If F is a field of type date range, the Rule parser returns for the error condition
DateRangesOverlap(F)
the following message
At 'DateRangesOverlap': There must be more than one field.
[MVK_PARAMSIZE_INVALIDFN]
See
Date range conditions
MVK_NO_DATE_RANGE
201

-- 201 of 261 --

Example
If F is a field of type string, the Rule parser returns for the error condition
StartOfDateRange(F) <= "11.11.2000"
the following message
At 'StartOfDateRange': 'F' is not a date range.
[MVK_NO_DATE_RANGE]
Background
With the language constructs
• StartOfDateRange
• EndOfDateRange
• DateRangesOverlap
• AtLeastOneDateRangeOverlaps
only fields of type DateRange may be used as arguments.
See
Date range conditions
MVK_ONLY_CONSTANTS_USED
Example
If F and G are fields of type Date, the Rule parser returns for the error condition
Valid(Date("01","01","1990"))
the following message
At 'Valid': date construction must not contain only constants.
[MVK_ONLY_CONSTANTS_USED]
Background
Date constructions that consist only of constants are permitted. However, it is not permitted to
apply the language constructs Valid and Invalid to them. This is not necessary because the validity
check for constant date components is already carried out when using the date constructor. For
example, the invalid month specification in
202

-- 202 of 261 --

Today < Date("01","13","1990")
leads to the parser message
At 'Date': Wrong date format at '13'.
[MVK_WRONG_DATE_FORMAT_FOR_OP]
See
• Date
• Valid and Invalid
MVK_DATE_OR_TIME_EXTRACT_OP_NOT_ALLOWED
Example
If F and G are fields of type Date, the Rule parser returns for the error condition
[F]!=Date(MonthFromDate(G),"11","2022")
the following message
At 'Date': at this position 'DayFromDate' is the only allowed extraction operator.
[MVK_INVALID_DATE_OR_TIME_EXTRACT_OP]
Background
If a date extraction operator is specified as the first parameter in a date construction, it must be
DayFromDate.
Solution
One solution may be to specify the date extraction operator MonthFromDate as the second parameter
in the date construction.
[F]!=Date("11",MonthFromDate(G),"2022")
See
Date
MVK_EXTRACT_OP_NOT_ALLOWED_4_DATE_CONSTRUCT_WITH_4_PARAMS
203

-- 203 of 261 --

Example
If F and G are fields of type Date, the Rule parser returns for the error condition
[F]!=Date("11","11",YearFromDate(G),"89")
the following message
At 'Date': at this position no extraction operator is allowed when specifying four
parameters.
[MVK_EXTRACT_OP_NOT_ALLOWED_4_DATE_CONSTRUCT_WITH_4_PARAMS]
Background
If four parameters are specified in a date construction (year consists of two parts), the system
checks that neither the third nor the fourth parameter is a date extraction operator.
Solution
Perform the date construction with three instead of four arguments.
[F]!=Date("1","11",G)
See
Date
MVK_MISSING_YEAR
Example
If Field F is of type date with the Error Condition
[F]!=Date("1","11")
the Rule parser returns the message
At 'Date': Missing year.
[MVK_MISSING_YEAR]
Background
To construct dates with Date(<Day>,<Month>), hence without a year specification, is not allowed.
204

-- 204 of 261 --

Solution
Specify a year as third argument, e.g.
[F]!=Date("1","11","2022")
See
Date
MVK_NO_DIGIT_PATTERN
Example
If F is a field of type string (without the pattern 'digit sequence') and G is a field of type Date, the
Rule parser returns for the error condition
Date("11","11",F) > [G]
the following message
At 'Date': The field 'F' must use the pattern 'numerical sequence'.
[MVK_NO_DIGIT_PATTERN]
Background
String fields that are used to construct date or time specifications must have the pattern 'digit
sequence'.
Solution
Assign the pattern \d+ to field F.
See
Date
MVK_NO_DATE
Example
If F is a field of type string, the Rule parser returns for the error condition
YearFromDate(F) <= 2023
the following message
205

-- 205 of 261 --

At 'YearFromDate': The field 'F' does not specify a date.
[MVK_NO_DATE]
Background
For language constructs that expect a date field as an argument, a date field must also be specified.
See
• Date
• Time
MVK_INVALID_DATE_STRING
Example
If F is a field that is of type date, the Rule parser returns for the error condition
AddDays("07.17.2020",1)==[F]
the following message
At 'AddDays':'07.17.2020' is not a valid date. Dates must be in the format dd.mm.,
dd.mm.yyyy., HH:mm:ss or dd.mm.yyyy'T'HH:mm:ss.
[MVK_INVALID_DATE_STRING]
Solution
Use a valid date specification such as
AddDays("17.07.2020",1)==[F]
See
• Date
• Time
MVK_INVALID_COMPARE_TO_DATE
Example
If F is a field that is not of type date the Rule parser returns for the error condition
[F]=="01.12.2020"
206

-- 206 of 261 --

the following message
It is only allowed to compare dates with other fitting dates.
[MVK_INVALID_COMPARE_TO_DATE]
Background
When comparing dates, all fields and constants must be of the date type.
See
• Date
• Time
MVK_INVALID_TIME
Example
If F is a field of type Date with format HH.mm:ss (i.e. a time specification), the Rule parser returns
for the error condition
AddYears(F,3) > "11.11.2000"
the following message
At 'AddYears': The 1. parameter needs to contain date specifications. Pure time
specifications are not allowed.
[MVK_INVALID_TIME]
See
• Date
• Time
MVK_DATE_WITH_AND_WITHOUT_YEAR
Example
Let F be a Field of type Date and let G be a Field of type Date Fragment with format MM-DD. For the
Rule condition
DifferenceInDays(F,G) < 100
the Rule parser returns
207

-- 207 of 261 --

At 'DifferenceInDays': All fields must either include or exclude the year.
[MVK_DATE_WITH_AND_WITHOUT_YEAR]
Background
For models without Base Year and the language constructs
• DifferenceInDays
• DifferenceInMonths
• DifferenceInYears
• DateRangesOverlap
• AtLeastOneDateRangeOverlaps
either all arguments must contain the year or none do. In models with a Base Year, however,
missing year specifications are replaced by the Base Year.
See
BaseYear
8.9. NumberOfValueInFields
MVK_NO_STRING_OR_ENUM
Example
In the model
with the Error Condition
NumberOfValueInFields("Berlin" In F,G) > 1
the Rule parser returns the message
At 'NumberOfValueInFields': It is only allowed to use strings or enumerations.
[MVK_NO_STRING_OR_ENUM]
208

-- 208 of 261 --

Background
With NumberOfValueInFields the types of the fields must match the constants used. The following
combinations are possible.
Constant Fields of type
Type String String, Enumeration (and mixed)
Type Number Number
True Boolean, Confirm (and mixed)
False Boolean
See
NumberOfValueInFields
MVK_NO_TYPE_YES_OR_YESNO
Example
In the model
with the Error Condition
NumberOfValueInFields(True In F,G)>1
the Rule parser returns the message
At 'NumberOfValueInFields': It is only allowed to use fields of type confirm or
boolean.
[MVK_NO_TYPE_YES_OR_YESNO]
Background
With NumberOfValueInFields the types of the fields must match the constants used. The following
combinations are possible.
209

-- 209 of 261 --

Constant Fields of type
Type String String, Enumeration (and mixed)
Type Number Number
True Boolean, Confirm (and mixed)
False Boolean
See
NumberOfValueInFields
MVK_NO_TYPEYESNO
Example
In the model
with the Error Condition
NumberOfValueInFields(False In F,G) > 1
the Rule parser returns the message
At 'NumberOfValueInFields': It is only allowed to use fields of type boolean.
[MVK_NO_TYPEYESNO]
Background
With NumberOfValueInFields the types of the fields must match the constants used. The following
combinations are possible.
Constant Fields of type
Type String String, Enumeration (and mixed)
Type Number Number
True Boolean, Confirm (and mixed)
False Boolean
210

-- 210 of 261 --

See
NumberOfValueInFields
MVK_INVALID_INTERVAL
Example
In the model
with the Error Condition
RangeAsString(K/F,0,3) == [G]
the Rule parser returns the message
At 'RangeAsString': The interval is not valid. The beginning of the interval must be
greater than 0 and less than the end of the interval.
[MVK_INVALID_INTERVAL]
Background
With
RangeAsString(K/F,k,l)
k and l must be integers with k > 1 and k < l.
See
RangeAsString and RangeAsNumber
8.10. Paths
MVK_INVALID_ROOT_GROUP
211

-- 211 of 261 --

Example
In a model that does not contain a rootgroup with the name A, and Error Condition
GroupNotFilled(/A)
the Rule parser returns the message
Rootgroup '/A' nicht gültig.
[MVK_INVALID_ROOT_GROUP]
Background
Only rootgroups that exist in the model can be referenced in the error condition.
See
Paths
MVK_INVALID_WILDCARD_REL
Example
In the model
with the Error Condition
AtLeastOneFieldFilled(..*/F) and GroupFilled(RuleGroup)
the Rule parser returns the message
In a relative path containing ..-notation, the '..' parts may not use the '*' symbol.
[MVK_INVALID_WILDCARD_REL]
212

-- 212 of 261 --

Solution
Instead, use the absolute path notation
AtLeastOneFieldFilled(/A*/F) and GroupFilled(RuleGroup)
See
• Absolute paths
• Relative paths
MVK_INVALID_ENTITY
Example
In the model
with the Error Condition
GroupFilled(L)
the Rule parser returns the message
The group or field 'L' is not valid.
[MVK_INVALID_ENTITY]
Background
Only groups and fields that exist in the model can be referenced in the error condition.
See
• Absolute paths
• Relative paths
213

-- 213 of 261 --

MVK_TOO_MANY_PARENT_GROUPS
Example
In the model
with the Error Condition
FieldFilled(../../../F)
the Rule parser returns the message
The relative path specification contains too many upward steps.
[MVK_TOO_MANY_PARENT_GROUPS]
Solution
Specify the correct relative path.
FieldFilled(../../F)
See
• Absolute paths
• Relative paths
8.11. Pattern
MVK_NO_STRING_OR_ENUM_OR_EXT_ENUM
Example
If F is a field that is not of type
• String or
214

-- 214 of 261 --

• Enumeration
the parser returns for an error condition such as
Invalid(F,"Email")
(where "Email" is an example of a predefined type) the following message
At 'Invalid': it is only allowed to use string, enumerations and extensible
enumerations.
[MVK_NO_STRING_OR_ENUM_OR_EXT_ENUM]
Background
You can use Valid and Invalid to check whether a field value is correct in relation to a predefined
field type. The field must be of type
• String or
• Enumeration
Solution
Change the type of field F to
• String or
• Enumeration
MVK_INVALID_PATTERN
Example
If F is a field of type string, the Rule parser returns for the error condition
[F] PatternViolated "\e+"
the following message
'\e+' is not a valid pattern to compare 'PatternViolated'.
[MVK_INVALID_PATTERN]
Background
The character string \e+ does not define a valid regular expression.
215

-- 215 of 261 --

Solution
Use a valid regular expression such as \d+.
See
PatternMatched and PatternViolated
MVK_INVALID_TYPE_FOR_PATTERN_COMPARISON
Example
If F is a field of type number, the Rule parser returns for the error condition
[F] PatternViolated "\d+"
the following message
Comparative operation 'PatternViolated' is only allowed for string values.
[MVK_INVALID_TYPE_FOR_PATTERN_COMPARISON]
Background
Comparisons with PatternMatched and PatternViolated are only permitted for fields of type string.
Solution
Use a field of type string for the comparison.
See
PatternMatched and PatternViolated
MVK_INVALID_PARAMETER_FOR_PATTERN_COMPARISON
Example
If F is a field of type string, the Rule parser returns for the error condition
[F] + "T" PatternViolated "\d+"
the following message
Comparative operation 'PatternViolated' is only allowed with a field value as first
parameter.
[MVK_INVALID_PARAMETER_FOR_PATTERN_COMPARISON]
216

-- 216 of 261 --

Background
A field value must appear on the left-hand side for comparisons with PatternMatched and
PatternViolated.
Solution
Use a field of type String for the comparison.
See
PatternMatched and PatternViolated
MVK_MISSING_CONST_IN_PATTERN_COMP
Example
If F and G fields are of type string, the Rule parser returns for the error condition
[F] PatternViolated [G]
the following message
The comparison 'PatternViolated' must have a string constant on the right side. This
string constant must specify a valid pattern.
[MVK_MISSING_CONST_IN_PATTERN_COMP]
Background
A regular expression must be used as the second argument for comparisons with PatternMatched
and PatternViolated.
See
PatternMatched and PatternViolated
8.12. Semantic Index
MVK_INDEX_VALUE_INVALID
Example
In the model
217

-- 217 of 261 --

with the Error Condition
FieldFilled(K/F for "Berlin")
the Rule parser returns the message
The specified value 'Berlin' for the index field '/A/K/Index' is invalid.
[MVK_INDEX_VALUE_INVALID]
Background
When using <field> for <constant> the constant must match the type of the index field.
Solution
Either use a number constant such as "1" or specify the string type for the index field.
See
Semantic Index
MVK_SEMANTIC_INDEX_NOT_ALLOWED
In many cases, fields in language constructs can also be used with the semantic index. There are
three exceptions to this:
• The use of semantic indices is not permitted with Having.
• The use of semantic indices is not permitted for NumberOfFilledGroups.
• The use of semantic indices is not permitted with RepetitionNotUnique.
See
Semantic Index
MVK_INDEX_TYPE_INCONSISTENT
Example
In the model
218

-- 218 of 261 --

with the Error Condition
FieldFilled(K/F for Ind)
the Rule parser returns the message
The type of the field 'Ind' does not match the type of the index field '/A/K/Index'.
[MVK_INDEX_TYPE_INCONSISTENT]
Background
When using <field1> for <field2> <field2> must match the type of the index field.
Solution
Align the field types of <field2> and index.
See
Semantic Index
MVK_SEMANTIC_INDEX_CONTAINED_IN_INDEX
Example
In the model
(the fields Person and ResponsiblePerson are of type Enumeration with values PersonA and
PersonB) and the Error Condition
219

-- 219 of 261 --

[Name for ResponsiblePerson] == "Richter"
the Rule parser returns the message
The field 'ResponsiblePerson' is used as semantic index, but it is contained in the
iterating group. This is not allowed.
[MVK_SEMANTIC_INDEX_CONTAINED_IN_INDEX]
Background
When using a field G as a semantic index, i.e. for a term of the type
F for G
G must not be contained in the group of the index field.
See
Semantic Index
MVK_SEMANTIC_INDEX_WITH_CATEGORY
Example
In the model
with enumeration fields
Field Country
value Category 'Continent'
Vietnam Asia
Germany Europe
and
220

-- 220 of 261 --

Field
Continen
t
value
Asien
Europa
with the Error Condition
[Multiplicator for Country -> Continent] == 20
the Rule parser returns the message
'Country' specifies a semantic index with a category access. This is not allowed.
[MVK_SEMANTIC_INDEX_WITH_CATEGORY]
Background
For fields as a semantic index, i.e. for a condition of the type
F for G
no category access is allowed for G. Same is true for the condition
F for G -> Kat
In contrast, a category may be accessed with F. The following is permissible
F -> Kat for G
See
• Semantic Index
• The field value operator
MVK_SEMANTIC_INDEX_AND_WILDCARD
Example
In the model
221

-- 221 of 261 --

with the Error Condition
[K*/F for "1"] >= 100
the Rule parser returns the message
Field 'K/F' uses a semantic index and uses an asterisk on the level of the index
field. This is not allowed.
[MVK_SEMANTIC_INDEX_AND_WILDCARD]
Background
K*/F is used to define a field list, whereas the semantic index for is used to access a specific instance
of F. These two modelling methods cannot be combined for the same repetition level.
Solution
Remove the asterisk:
[K/F for "1"] >= 100
See
• Asterisk operator
• Semantic Index
MVK_SEMANTIC_INDEX_NO_FIELD
Example
If K is a group, the Rule parser returns for the error condition
FieldFilled(F for K)
the following message
222

-- 222 of 261 --

'K' is used as semantic index but is not a field.
[MVK_SEMANTIC_INDEX_NO_FIELD]
Background
Only fields are permitted as a semantic index. When using F1 for F2, F2 must be a field. F2 must
not be a group.
See
Semantic Index
MVK_SEMANTIC_INDEX_WITH_WILDCARD
Example
In the model
with the Error Condition
FieldFilled(K/F for /A*/Index)
the Rule parser returns the message
Field '/A/Index' is used as semantic index and therefore must not specify an asterisk.
[MVK_SEMANTIC_INDEX_WITH_WILDCARD]
Background
No asterisk is permitted for a semantic index. When using F1 for F2 F2 must not have an asterisk.
Solution
Omit the asterisk and use the condition
223

-- 223 of 261 --

FieldFilled(K/H for Index)
See
Semantic Index
MVK_SEMANTIC_INDEX_WITH_SEMANTIC_INDEX
Example
For the Error Condition
FieldFilled(F for G for H)
the Rule parser returns the message
Field 'G' specifies a semantic index and therefore must not use a semantic index
itself.
[MVK_SEMANTIC_INDEX_WITH_SEMANTIC_INDEX].
Background
A nested use of the semantic index as in the example above is not permitted.
See
Semantic Index
MVK_INDEX_FIELD_INVALID
Example
In the model
with the Error Condition
FieldFilled(K/F for "1")
224

-- 224 of 261 --

the Rule parser returns the message
For 'K/F' the used index field 'Index' does not exist.
[MVK_INDEX_FIELD_INVALID]
Background
An index field 'Index' was specified for group K which does not even exist as a field. This might
happen when you delete an index field and forget to remove the specification as an index field as
well.
Solution
Create a suitable index field 'Index'.
See
Semantic Index
MVK_NO_INDEX_FIELD
Example
In the model
the Index field is not specified as an index field. For the Error Condition
FieldFilled(K/F for "1")
the Rule parser returns the message
For 'K/F' there is no repeatable group with index field.
[MVK_NO_INDEX_FIELD]
Background
To be able to use the 'Index' field as a semantic index, it must be specified as an index field of group
K. The same message is displayed if the Index field does not exist at all.
225

-- 225 of 261 --

Solution
Specify the field Index as the index field of group K.
See
Semantic Index
8.13. Field Types
MVK_ONLY_STRING_ENUM_NUMBER_DATE_ALLOWED
Example
If F and G are fields of type confirm, the Rule parser returns for the error condition
FieldValuesNotUnique(F,G)
the following message
At 'FieldValuesNotUnique': It is only allowed to use string fields, enumerations,
numbers (including amounts) and date fields with the same dateformat. Corrupt: 'F'.
[MVK_ONLY_STRING_ENUM_NUMBER_DATE_ALLOWED]
See
Predicate language constructs
MVK_INVALID_TYPES_FOR_COMPARISON
Example
If F is a field of type number, the Rule parser returns for the error condition
FieldValueNotIncludedInValueList(F,"a","b")
the following message
At 'FieldValueIncludedInValueList': It is not allowed to compare strings and numbers.
[MVK_INVALID_TYPES_FOR_COMPARISON].
Background
With the language constructs
• FieldValueIncludedInValueList
• FieldValueNotIncludedInValueList
226

-- 226 of 261 --

• NoFieldValueIncludedInValueList
• AtLeastOneFieldValueIncludedInValueList
• NotAllFieldValuesIncludedInValueList
strings must not be compared with numbers.
See
Predicate language constructs
MVK_ONLY_STRING_ENUM_NUMBER_ALLOWED
Example
If F is a field of type date, the Rule parser returns for the error condition
FieldValueNotIncludedInValueList(F,1,2)
the following message
At 'FieldValueNotIncludedInValueList': It is only allowed to use string fields,
enumerations or numbers (including amounts). Corrupt: 'F'.
[MVK_ONLY_STRING_ENUM_NUMBER_ALLOWED]
Background
With the language constructs
• FieldValueIncludedInValueList
• FieldValueNotIncludedInValueList
• NoFieldValueIncludedInValueList
• AtLeastOneFieldValueIncludedInValueList
• NotAllFieldValuesIncludedInValueList
only the following field types are permitted
• String
• Enumeration
• Number
See
Predicate language constructs
227

-- 227 of 261 --

MVK_INVALID_TYPE
Example
If F is a field of type Number, and G is a field of type String, the Rule parser returns for the error
condition
[G] == RangeAsString(F,2,4)
the following message
At 'BereichAlsString': Invalid type. 'F' must be either a string, a predefined type or
an extensible enumeration.
[MVK_INVALID_TYPE].
Background
RangeAsString can only be used with the field type string.
See
RangeAsString and RangeAsNumber
MVK_INVALID_COMPARE_TO_YES
Example
If the field F is of type confirm, the Rule parser returns for
[F] == False
the following message:
It is only allowed to compare equality of a field of type confirm with another field
of type confirm or to compare equality or unequality with the constant True.
[MVK_INVALID_COMPARE_TO_YES]
Background
A field of type confirm can be compared with another field of type confirm or with the constant
True using the comparison operators == and !=. A comparison with the constant False is not
permitted. The same message is returned if a confirm field is compared with a constant other than
True, e.g.
[F] == 7
228

-- 228 of 261 --

[F] != "USA"
or any other term that is not of type confirm
[F] == [G]
[F] != FirstFilledValue(A*/G)
Solution
The condition that the confirm field F is not specified can be expressed with
FieldNotFilled(F)
See
Confirm
MVK_INVALID_COMPARE_TO_YESNO
Example
If F is a field of type boolean, the parser returns for the error condition
[F]==7
the following message
It is only allowed to compare equality or unequality of a field of type boolean with
another field of type boolean or with the constants True or False.
[MVK_INVALID_COMPARE_TO_YESNO]
See
Boolean
MVK_INVALID_COMPARE_TO_DATE_RANGE
Example
For fields
Field Type Format
F DateRange YYYY-MM-DD/YYYY-MM-DD
229

-- 229 of 261 --

Field Type Format
G DateRange YYYY-MM/YYYY-MM
and error condition
[F]!=[G]
the following message
It is only allowed to compare date ranges with other fitting date ranges.
[MVK_INVALID_COMPARE_TO_DATE_RANGE]
is returned by the Rule parser.
Background
Date ranges may be compared with == or with !=. However, they must have the same format.
See
• Comparison operations
• Comparing Date Ranges
MVK_INVALID_COMPARE_TO_ENUM_OR_STRING
Example
If F is a field of type string and G is a field of type number, for the error condition
[F]==[G]
the following message
It is only allowed to compare a character string with another character string.
[MVK_INVALID_COMPARE_TO_ENUM_OR_STRING]
is returned.
Background
When comparing with == and != terms of type string may only be compared with other terms of
type string. Terms of type string are:
• String constants
• Values of fields of type String, Enumeration, Unfinished Enumeration (including type definitions
230

-- 230 of 261 --

based on String, Enumeration, Unfinished Enumeration) and fields of type Predefined
• Return values of the operators FieldValueAsString and RangeAsString
• Return values from string concatenation using +
See
• Field types
• Comparison operations
Solution
One possible solution is to convert the field value of G into a string before the comparison using ==.
[F]==FieldValueAsString(G)
MVK_INVALID_STRING_CONSTANT_FOR_ENUM_COMPARISON
Example
If the Person field is an enumeration field with enumeration values PersonA and PersonB, for the
error condition
[Person]=="PersonC"
the following message
'PersonC' is not valid in the used enumeration.
[MVK_INVALID_STRING_CONSTANT_FOR_ENUM_COMPARISON]
is returned.
Background
When comparing the value of a field of type enumeration with a constant, this constant must be an
enumeration value of this field.
See
Enumeration Comparison operations
Solution
Replace the string "PersonC" with an enumeration value of the Person field:
[Person]=="PersonB"
231

-- 231 of 261 --

MVK_NO_NUMBER
Example
If F a field of type string, the Rule parser returns for the error condition
AbsValue(F) > 0
the following message
At 'AbsValue': Only numbers and amounts are allowed to be used.
[MVK_NO_NUMBER]
Background
There are a number of language constructs that only allow numbers or amounts as arguments:
• At 'AbsValue': Only numbers and amounts are allowed to be used. [MVK_NO_NUMBER]
• At 'SumOfProducts': Only numbers and amounts are allowed to be used. [MVK_NO_NUMBER]
• At 'FeldWertAlsString': Only numbers and amounts are allowed to be used. [MVK_NO_NUMBER]
• At 'Abs': Only numbers and amounts are allowed to be used. [MVK_NO_NUMBER]
• At 'RoundAccountingValue': Only numbers and amounts are allowed to be used.
[MVK_NO_NUMBER]
• At 'RoundUpValue': Only numbers and amounts are allowed to be used. [MVK_NO_NUMBER]
• At 'RoundDownValue': Only numbers and amounts are allowed to be used. [MVK_NO_NUMBER]
• At 'RoundAccounting': Only numbers and amounts are allowed to be used. [MVK_NO_NUMBER]
• At 'RoundUp': Only numbers and amounts are allowed to be used. [MVK_NO_NUMBER]
• At 'RoundDown': Only numbers and amounts are allowed to be used. [MVK_NO_NUMBER]
This parser message can also occur in connection with 'NumberOfValueInFields'.
Example
In the model
with the Error Condition
232

-- 232 of 261 --

NumberOfValueInFields(1 In F,G) > 1
the Rule parser returns the message
At 'NumberOfValueInFields': Only numbers and amounts are allowed to be used.
[MVK_NO_NUMBER]
Background
With NumberOfValueInFields the types of the fields must match the constants used. The following
combinations are possible.
Constant Fields of type
Type String String, Enumeration (and mixed)
Type Number Number
True Boolean, Confirm (and mixed)
False Boolean
See
NumberOfValueInFields
MVK_NO_BOOLY_ALLOWED
Example
If F and G are fields of type confirm, the Rule parser returns for the error condition
FirstFilledValue(F, G) == True
the following message
At 'ErsterAngegebenerWert' no confirm fields are allowed. Incompatible: 'F'.
[MVK_NO_BOOLY_ALLOWED]
See
FirstFilledValue
MVK_VARYING_TYPES_NOT_ALLOWED
Example
If F1 is a field of type string and F2 is a field of type number, the Rule parser returns for the error
condition
233

-- 233 of 261 --

FieldValuesNotUnique(F1,F2)
the following message
At 'FieldValuesNotUnique': All fields must be the same type.
[MVK_VARYING_TYPES_NOT_ALLOWED]
Background
With the language constructs
• FieldValuesNotUnique
• FirstFilledValue
• FieldValueIncludedInValueList
• FieldValueNotIncludedInValueList
• NoFieldValueIncludedInValueList
• AtLeastOneFieldValueIncludedInValueList
• NotAllFieldValuesIncludedInValueList
• NumberOfValueInFields
all specified fields must be of the same type.
See
Predicate language constructs for field lists
MVK_INCONSISTENT_TYPES_COMPARED
Example
If F is a field of type number, the Rule parser returns for the error condition
[F] == True
the following message
Invalid comparison.
[MVK_INCONSISTENT_TYPES_COMPARED]
Background
A number cannot be compared with a Boolean value.
234

-- 234 of 261 --

8.14. Number and Uniqueness of Arguments
MVK_DUPLICATE_PARAM1
Example
If F and G are fields, the Rule parser returns for the error condition
AtLeastOneFieldFilled(F,G,G)
the following message
At 'AtLeastOneFieldFilled': 'G' was defined more than once.
[MVK_DUPLICATE_PARAM1]
Background
A field may not appear more than once in a field list, nor may a group appear more than once in a
field list.
Solution
Use the following Error Condition instead
AtLeastOneFieldFilled(F,G)
See
Field lists
MVK_DUPLICATE_PARAM2
Example
If F is a field that is directly or indirectly contained in a group K, the Rule parser returns for the
error condition
AtLeastOneFieldFilled(K/F,K)
the following message
At 'AtLeastOneFieldFilled': 'K/F' was defined directly and via a group.
[MVK_DUPLICATE_PARAM2]
235

-- 235 of 261 --

Background
A field may not appear more than once in a field list - not even via different types of referencing
(e.g. directly and indirectly via a group).
Solution
Use the following Error Condition instead
AtLeastOneFieldFilled(K)
See
Field lists
MVK_PARAMSIZE_INVALID1
Example
If F and G are fields, the Rule parser returns for the error condition
FieldFilled(F1,F2)
the following message
'FieldFilled' must contain exactly one parameter.
[MVK_PARAMSIZE_INVALID1]
Background
With the language constructs
• FieldFilled
• FieldNotFilled
• GroupFilled
• GroupNotFilled
exactly one parameter must be specified.
See
Predicate language constructs
MVK_PARAMSIZE_INVALID2
236

-- 236 of 261 --

Example
With the error conditions
AllGroupsFilled(K)
and
GroupsNotCollectivelyFilled(K)
the Rule parser returns the message
At 'AllGroupsFilled' There must be at least two groups or fields.
[MVK_PARAMSIZE_INVALID2]
Background
The language constructs AllGroupsFilled(K) and GroupsNotCollectivelyFilled(K) expect a list of
groups as parameters that contains at least two elements. A single group is not sufficient.
See
Predicate language constructs for groups
MVK_PARAMSIZE_INVALIDN
Example
If F is a field, the Rule parser returns for the error condition
NumberOfFilledFields(F)
the following message
At 'NumberOfFilledFields': There must be more than one field or at least one group
defined.
[MVK_PARAMSIZE_INVALIDN]
is returned.
Background
There are a number of language constructs that expect a field list as an argument and for which a
single field is not permitted as an argument:
• NotAllFieldsFilled
237

-- 237 of 261 --

• AllFieldsFilled
• FieldsNotCollectivelyFilled
• NotExactlyOneFieldFilled
• MoreThanOneFieldFilled
• FieldValuesNotUnique
• MaxValue
• MinValue
• Sum
• NumberOfFilledFields
• NumberOfDifferentValues
See
• Predicate language constructs for field lists
• Arithmetic language constructs for value lists
MVK_PARAMSIZE_INVALIDGN
Example
If K is a group, the Rule parser returns for the error condition
NumberOfFilledGroups(K)==1
the following message
At 'NumberOfFilledGroups': There must be more than one group.
[MVK_PARAMSIZE_INVALIDGN]
See
NumberOfFilledGroups
8.15. Asterisk Operator
MVK_INVALID_WILDCARD_FOR_RULEGROUP
Example
In the model
238

-- 238 of 261 --

with the Error Condition
AllFieldsFilled(RuleGroup*/F)
the Rule parser returns the message
It is not allowed to specifiy an asterisk for 'RuleGroup'.
[MVK_INVALID_WILDCARD_FOR_RULEGROUP]
Solution
Modify the error condition
AllFieldsFilled(/A/K*/F)
See
Asterisk operator
MVK_NO_WILDCARD
Example
In the model
with the Error Condition
239

-- 239 of 261 --

Sum(K*/L/F) > 10
the Rule parser returns the message
Missing specification of an asterisk for 'K/L' in 'K/L/F'.
[MVK_NO_WILDCARD]
Background
If a repeatable group in the path is marked with an asterisk, all repeatable groups below it in the
path must also be marked with an asterisk.
Solution
Group L is also marked with an asterisk.
Sum(K*/L*/F) > 10
See
Asterisk operator
MVK_INVALID_WILDCARD
Example
In the model
with the Error Condition
AllFieldsFilled(F, K*/G)
the Rule parser returns the message
240

-- 240 of 261 --

Since 'K' is not repeatable, it is not allowed to specify an asterisk.
[MVK_INVALID_WILDCARD]
Background
The asterisk operator is only useful and permitted for repeatable groups.
Solution
Omit the asterisk.
AllFieldsFilled(F, K/G)
See
Asterisk operator
MVK_NO_WILDCARDS_ALLOWED
Example
In the model
with the Error Condition
FieldFilled(/A*/F)
the Rule parser returns the message
At 'FieldFilled': It is not allowed to use an asterisk.
[MVK_NO_WILDCARDS_ALLOWED].
Background
There are a number of situations in which no asterisk may be specified.
1. For language constructs that expect a single field as an argument:
241

-- 241 of 261 --

◦ Field value operator [ ]
◦ AbsValue
◦ Length
◦ FieldValueAsNumber
◦ FieldValueAsString
◦ RoundDownValue
◦ RoundUpValue
◦ RoundAccountingValue
◦ RangeAsNumber
◦ RangeAsString
◦ StartOfDateRange
◦ EndOfDateRange
◦ ValueAsDate
2. For language constructs that expect a single field as the first argument:
◦ FieldValueIncludedInValueList
◦ FieldValueNotIncludedInValueList
3. For language constructs that expect a single group as an argument:
◦ GroupFilled
◦ GroupNotFilled
4. For language constructs that expect a single field or a single group as an argument:
◦ CurrentRepetition
5. For language constructs that allow a field list as an argument, but which may not contain an
asterisk:
◦ RepetitionNotUnique
See
Asterisk operator
MVK_NO_WILDCARDS_G_ALLOWED
Example
In the model
242

-- 242 of 261 --

with the Error Condition
AllGroupsFilled(/A/K*)
the Rule parser returns the message
At 'AllGroupsFilled': It is not allowed to use an asterisk for any group.
[MVK_NO_WILDCARDS_G_ALLOWED]
Background
For the language constructs
• NotAllGroupsFilled
• AllGroupsFilled
• GroupsNotCollectivelyFilled
the usage of the asterisk operator is not allowed.
Solution
Use the following Error Condition instead
NumberOfFilledGroups(/A/K*) == 10
See
• Asterisk operator
• Predicate language constructs for groups
8.16. Filter Operator and SumOfTerms
MVK_WILDCARDS_REQUIRED
243

-- 243 of 261 --

Example
In the model
with the Error Condition
Sum(K/Amount Having [K/Type]=="Share") >= 1000
(where 'Share' is an enumeration value of the field Type) the Rule parser returns the message
At 'Having': At least one asterisk needs to be specified for the filtered field.
[MVK_WILDCARDS_REQUIRED]
Background
The filter operator Having requires a field list that is defined with an asterisk.
Solution
Insert an asterisk
Sum(K*/Amount Having [K/Type]=="Share") >= 1000
See
Filter Operator
MVK_WILDCARD_AT_LOWEST_LEVEL_REQUIRED
Example
In the Model
244

-- 244 of 261 --

with Error Condition
SumOfTerms(Shares/L,[Share_Numerator]/[Share_Denominator]) > 0.5
the Rule parser returns
At 'Shares/L': The lowest level must specify an asterisk.
[MVK_WILDCARD_AT_LOWEST_LEVEL_REQUIRED]
Background
When using SumOfTerms, the lowest Group must be repeatable and have an asterisk. But 'Shares/L'
does not have a star.
Solution
Write an asterisk after the lowest group L:
SumOfTerms(Shares/L*,[Share_Numerator]/[Share_Denominator]) > 0.5
Alternatively, write asterisks both after Shares and L:
SumOfTerms(Shares*/L*,[Share_Numerator]/[Share_Denominator]) > 0.5
See
SumOfTerms
245

-- 245 of 261 --

MVK_NO_ITERATION_FOR_WILDCARD
Example
In the model
with the Error Condition
Sum(K*/G Having FieldFilled(F)) <= 10
the Rule parser returns the message
In filter conditions, iteration must be specified for at least one level, that
specified asterisk for the filtered field.
[MVK_NO_ITERATION_FOR_WILDCARD]
Background
In filter conditions, you must iterate over at least one level with an asterisk of the filtered field. In
the example above, this means that a field located in K must be referenced in the filter condition.
Solution
The rule in the example above becomes correct if you move the field F to the group K.
See
Filter Operator
MVK_NO_ITERATION_FOR_LOWEST_WILDCARD
Example
In the Model
246

-- 246 of 261 --

with the Error Condition
SumOfTerms(Shares*/L*,[Share_Numerator]/[Share_Denominator]) > 0.5
the Rule parser returns the message
At 'SumOfTerms': iteration must be specified on the lowest level of the path with
asterisk.
[MVK_NO_ITERATION_FOR_LOWEST_WILDCARD]
Background
The term [Share_Numerator]/[Share_Denominator] does not iterate over the Group L.
Solution
Omit the lowest Group L in the path:
SumOfTerms(Shares*,[Share_Numerator]/[Share_Denominator]) > 0.5
See
SumOfTerms
MVK_PARALLEL_ITERATION_FOR_PATH_WITH_ASTERISKS_NOT_ALLOWED
Example
In the model
247

-- 247 of 261 --

with the Error Condition
AtLeastOneFieldFilled(K/M*/F Having FieldFilled(K/M/G)) and [K/H]==[L/H]
the Rule parser returns the message
At 'Having': parallel iteration is not allowed for the filtered entity.
[MVK_PARALLEL_ITERATION_FOR_PATH_WITH_ASTERISKS_NOT_ALLOWED]
Background
The filter operator must not be combined with parallel iteration.
See
Filter Operator
MVK_DISALLOWED_CONSTRUCT_IN_SET_EXPRESSION
Example
In the model
248

-- 248 of 261 --

with the Error Condition
AtLeastOneFieldFilled(L*/F Having FieldFilled(G) and CustomCondition CheckFails)
the Rule parser returns the message
The language feature 'CustomCondition' is not supported for filter conditions.
[MVK_DISALLOWED_CONSTRUCT_IN_SET_EXPRESSION]
Background
For both Filter Conditions and SumOfTerms
• the Filter Operator
• the language construct SumOfTerms
• the language construct CustomCondition
are not supported.
See
• Filter Operator
• SumOfTerms
MVK_INVALID_ITERATION_IN_SET_EXPRESSION
Example 1
In the model
249

-- 249 of 261 --

with the Error Condition
Sum(K*/G Having [K/F1]==[L/F2]) <= 10
the Rule parser returns the message
Error for 'L' in 'L/F2': in filter conditions iterations are only allowed on levels,
that specify an asterisk for the filtered field.
[MVK_INVALID_ITERATION_IN_SET_EXPRESSION]
Background
In filter conditions, you may only iterate over the repetition levels on which the filtered field
specifies iteration or asterisk. In the example above, you may only iterate over group K. However,
the field F2 iterates over the group L. This is only permitted if the dollar operator is used.
Solution
Use the dollar operator for the F2 field:
Sum(/A/K*/G Having [/A/K/F1]==[$L/F2]) <= 10
See
Filter Operator
Example 2
In the model
250

-- 250 of 261 --

with the Error Condition
SumOfTerms(K*,[K/Share_Numerator]/[L/Share_Denominator]) > 0.5
the Rule Parser returns the message
Error for 'L' in 'L/Share_Denominator': with 'SumOfTerms' iterations are only allowed
on levels, that specify an asterisk for the path with asterisks.
[MVK_INVALID_ITERATION_IN_SET_EXPRESSION]
Background
When using SumOfTerms, the term may only iterate over Groups which are on the path with asterisks
(as an absolute path). Thus, in Example 2, iteration may only take place over the Group K. But the
Field Share_Denominator iterates over the Group L.
Solution
Move the Field Share_Denominator into the Group K and use
SumOfTerms(K*,[K/Share_Enumerator]/[K/Share_Denominator]) > 0.5
as Error Condition.
See
SumOfTerms
MVK_INVALID_OUTER_ITERATION
Example
For a rule with a partial condition of
FieldFilled($F)
251

-- 251 of 261 --

which is not part of a filter condition, the Rule parser returns the message
The symbol '$' to specify rule iteration is only allowed inside filter conditions.
[MVK_INVALID_OUTER_ITERATION]
See
Iteration in filter condition
8.17. Other
MVK_NO_CONVERT_IN_NUMBER
Example
If F is a field of type string (without pattern), the Rule parser returns for the error condition
FieldValueAsNumber(F) == 10
the following message
FieldValueAsNumber': The field must either be type string or enumeration, whose
values, or category values, can always be transformed to a number. Corrupt: 'F'.
[MVK_NO_CONVERT_IN_NUMBER]
Background
The language construct FieldValueAsNumber expects as argument either
• a field of type string
• with pattern [0-9]+ and
• maximum length less than 16
or
• a field of the type enumeration
• with enumeration values which can be converted into numbers (maximum 15-digit numbers,
possibly also with sign and decimal places)
or
• the category access operator F-> K
• where it must be possible to convert the category values of K into numbers (maximum 15-digit
numbers, possibly also with sign and decimal places).
252

-- 252 of 261 --

Solution
Adjust the field type of F accordingly.
See
FieldValueAsString and FieldValueAsNumber
MVK_NO_GROUPS_ALLOWED
The argument of the field value operator [ ]
• must be a field (possibly together with semantic index For or access operator for categories ->)
• must not be a group.
If a group is used as the argument of the field value operator, e.g. by
[G] > 0
the Rule parser returns the following message:
At 'Feldwert-Operation': It is not allowed to use a group.
[MVK_NO_GROUPS_ALLOWED]
Background
In many situations, no group may be specified as an argument. This applies to the following:
• Field value operator [ ]
• Access operator for categories ->
• Having
• FieldFilled
• FieldNotFilled
• DateRangesOverlap
• FieldValueIncludedInValueList
• FieldValueNotIncludedInValueList
• RepetitionNotUnique
• Valid
• Invalid
• SumOfProducts
• AbsValue
• Length
• FieldValueAsNumber
253

-- 253 of 261 --

• FieldValueAsString
• RoundDownValue
• RoundUpValue
• RoundAccountingValue
• RangeAsNumber
• RangeAsString
• Date
MVK_ROOT_GROUP_REFERENCED
Example
For the Error Condition
GroupsNotCollectivelyFilled(/A,/B)
the error message
At 'GroupsNotCollectivelyFilled': It is not allowed to reference a rootgroup.
[MVK_ROOT_GROUP_REFERENCED]
is returned.
Background
The referencing of a rootgroup is only possible for the language constructs
• GroupFilled
• GroupNotFilled
• AtLeastOneGroupFilled
• NoGroupFilled
• NumberOfFilledGroups
Furthermore, the rootgroup must be the only parameter.
In the model
254

-- 254 of 261 --

the following conditions are permitted:
GroupFilled(/A)
AtLeastOneGroupFilled(/A*)
NoGroupFilled(/A*)
NumberOfFilledGroups(/A*)>=2
In the model
the condition
GroupNotFilled(/A)
is permitted.
See
• Predicate language constructs for groups
• Functional language constructs for field lists and group lists
255

-- 255 of 261 --

MVK_ROOT_GROUP_WITH_OTHER_PARAMETERS
Example
In the model
with the Error Condition
NumberOfFilledGroups(/A,K) > 2
the Rule parser returns the message
At 'AnzahlAngegebenerKontexte': Only a rootgroup is allowed to be used as parameter.
[MVK_ROOT_GROUP_WITH_OTHER_PARAMETERS]
Background
The referencing of a rootgroup is only possible for the language constructs
• GroupFilled
• GroupNotFilled
• AtLeastOneGroupFilled
• NoGroupFilled
• NumberOfFilledGroups
Furthermore, the rootgroup must be the only parameter.
In the model
256

-- 256 of 261 --

the following conditions are permitted:
GroupFilled(/A)
AtLeastOneGroupFilled(/A*)
NoGroupFilled(/A*)
NumberOfFilledGroups(/A*)>=2
In the model
the condition
GroupNotFilled(/A)
is permitted.
See
• Predicate language constructs for groups
• Functional language constructs for field lists and group lists
257

-- 257 of 261 --

MVK_NO_FIELDS_ALLOWED
Example
If F is a field, the Rule parser returns for the error condition
GroupFilled(F)
the following message
At 'GroupFilled': Only groups or rootgroups are allowed. Corrupt: 'F'.
[MVK_NO_FIELDS_ALLOWED]
See
Predicate language constructs
MVK_NO_EMPTY
Example
If F is a field of type string, the Rule parser returns for the error condition
Valid(F,"")
the following message
At 'Valid': the second parameter may not be empty.
[MVK_NO_EMPTY]
Background
If the language constructs Valid and Invalid are used to check for a predefined type, this must be
specified. The empty string "" is not permitted as the second argument.
See
Valid and Invalid for the evaluation of custom types
MVK_INVALID_SUPPRESSED_WARNING
If the addition
@SuppressWarning(MVK_INVALID_COMPARE_DEC_PLACES)
is used this causes the corresponding check to be suppressed. The condition
258

-- 258 of 261 --

@SuppressWarning(MVK_INVALID_COMPARE_DEC_PLACES)
FieldFilled(F) and [F]!=1.5
is also correct if no decimal places are specified for the field F. However, this suppression is only
possible for MVK_INVALID_COMPARE_DEC_PLACES. In all other cases, a corresponding error message is
returned. For the error condition
@SuppressWarning(MVK_NO_NUMBER)
the following message is displayed
The warning to be suppressed 'NO_NUMBER' is not supported.
[MVK_INVALID_SUPPRESSED_WARNING]
MVK_ROOT_GROUP_IS_EMPTY
Example
In the model
with the Error Condition
FieldFilled(F) and GroupNotFilled(/A)
the Rule parser returns the error
The rootgroup '/A' does not consist of any (valid) fields and is therefore not allowed
to be used in the rule.
[MVK_ROOT_GROUP_IS_EMPTY]
259

-- 259 of 261 --

Background
Rootgroups, that do not contain any fields (either directly or indirectly) may not be referenced in
error conditions.
MVK_GROUP_IS_EMPTY
Example
In the model
with the Error Condition
FieldFilled(F) and GroupNotFilled(K)
the Rule parser returns the error
Group 'K' does not contain (valid) fields and is therefore not allowed to be used in
rules.
[MVK_GROUP_IS_EMPTY]
Background
Groups, that do not contain any fields (either directly or indirectly) may not be referenced in error
conditions.
MVK_INVALID_COMPARE_TO_CONST
Example
If an error condition contains e.g. the partial condition
1==1
the Rule parser returns the message
It is not allowed to compare two constants.
260

-- 260 of 261 --

[MVK_INVALID_COMPARE_TO_CONST]
Background
Constants must not be compared with each other.
8.18. Parser Messages for Computation Rules
MVK_ERROR_REFERENCE_TO_CALCULATED_FIELD
Example
If F is a Field of type number, and a computation is given by
Precondition Calculation
FieldNotFilled(F) 10
then the Rule Parser return the message
The calculated field cannot be used in prerequisites and calculation functions.
[MVK_ERROR_REFERENCE_TO_CALCULATED_FIELD]
Background
The calculated field may not be referenced, either directly or indirectly, in the precondition.
Exception: An indirect reference of the calculated field in the precondition by means of the
language construct GroupFilled is permitted.
See
Preconditions
261

-- 261 of 261 --

