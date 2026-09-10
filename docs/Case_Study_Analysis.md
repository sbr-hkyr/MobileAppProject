# Case Study Analysis

## Problem

Vancomycin therapeutic drug monitoring requires patient characteristics, dosing details, renal function, and measured concentrations to be considered together. Manual calculation can be slow and difficult to review.

## Proposed solution

TDM Insight is an Android educational calculator that organizes these inputs, validates them, calculates pharmacokinetic values, and explains the calculation steps.

## Target users

The application is intended for students and educators studying therapeutic drug monitoring. It uses fictional cases and is not a clinical decision tool.

## Implemented features

- Pre-dose, post-dose, and combined workflows
- Input validation with errors and warnings
- Pharmacokinetic results and calculation explanations
- Fictional example cases
- Local calculation history
- Camera/gallery OCR workflow for laboratory reports
- Light and dark sage-green themes

## Technology and architecture

The project uses Kotlin, Jetpack Compose, MVVM, a dedicated calculation engine, Room for local history, and Google ML Kit text recognition.
