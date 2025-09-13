import jsPDF from 'jspdf';
import 'jspdf-autotable';
import ExcelJS from 'exceljs';
import type {
  ProgramWithWorkouts,
  ProgrammedWorkoutWithStages,
  WorkoutStageWithExercises,
  ProgrammedExerciseWithSetSchemes,
  SetScheme,
  Exercise,
  UserWeightUnitPreference,
} from '../api/types';
import { formatWeightWithUnit } from '../common/utils';

/**
 * Export options for different export types
 */
export interface ExportOptions {
  title: string;
  filename: string;
  includeCharts?: boolean;
}

/**
 * Export a single workout to PDF
 */
export const exportWorkoutToPDF = async (
  workoutData: ProgrammedWorkoutWithStages,
  exerciseData: Map<string, Exercise>,
  weightUnitPreferences: UserWeightUnitPreference[],
  options: ExportOptions
): Promise<void> => {
  const pdf = new jsPDF('p', 'mm', 'a4');
  
  // Add title
  pdf.setFontSize(18);
  pdf.setFont('helvetica', 'bold');
  pdf.text(options.title, 20, 20);
  
  // Add workout info
  pdf.setFontSize(12);
  pdf.setFont('helvetica', 'normal');
  pdf.text(`Day: ${workoutData.workout.day_number}`, 20, 30);
  pdf.text(`Created: ${new Date(workoutData.workout.created_at).toLocaleDateString()}`, 20, 35);
  pdf.text(`Total Stages: ${workoutData.stages.length}`, 20, 40);
  
  // Prepare table data
  const tableData: string[][] = [];
  const tableHeaders = ['Stage', 'Exercise', 'Sets', 'Reps', 'Weight', 'Rest (s)', 'Notes'];
  
  workoutData.stages.forEach((stage: WorkoutStageWithExercises, stageIndex: number) => {
    stage.exercises.forEach((exerciseWithSchemes: ProgrammedExerciseWithSetSchemes) => {
      const weightUnit = weightUnitPreferences.find(
        pref => pref.exercise_name === exerciseWithSchemes.exercise.exercise_name
      )?.preferred_unit || 'lbs';

      exerciseWithSchemes.set_schemes.forEach((setScheme: SetScheme) => {
        tableData.push([
          (stageIndex + 1).toString(),
          exerciseWithSchemes.exercise.exercise_name,
          setScheme.set_number.toString(),
          setScheme.target_rep_count?.toString() || '',
          formatWeightWithUnit(setScheme.target_weight || 0, weightUnit as 'KG' | 'LBS'),
          setScheme.rest_seconds?.toString() || '',
          ''
        ]);
      });
    });
  });

  // Add table using autoTable
  (pdf as any).autoTable({
    head: [tableHeaders],
    body: tableData,
    startY: 50,
    styles: {
      fontSize: 10,
      cellPadding: 3,
    },
    headStyles: {
      fillColor: [66, 139, 202],
      textColor: 255,
      fontStyle: 'bold',
    },
    alternateRowStyles: {
      fillColor: [245, 245, 245],
    },
    columnStyles: {
      0: { cellWidth: 15 }, // Stage
      1: { cellWidth: 50 }, // Exercise
      2: { cellWidth: 15 }, // Sets
      3: { cellWidth: 15 }, // Reps
      4: { cellWidth: 20 }, // Weight
      5: { cellWidth: 15 }, // Rest
      6: { cellWidth: 30 }, // Notes
    },
    margin: { left: 20, right: 20 },
  });

  pdf.save(`${options.filename}.pdf`);
};

/**
 * Export workout data to XLSX format
 */
export const exportWorkoutToXLSX = async (
  workoutData: ProgrammedWorkoutWithStages,
  exerciseData: Map<string, Exercise>,
  weightUnitPreferences: UserWeightUnitPreference[],
  options: ExportOptions
): Promise<void> => {
  const workbook = new ExcelJS.Workbook();
  
  // Create workout summary sheet
  const summarySheet = workbook.addWorksheet('Summary');
  summarySheet.addRow(['Workout Name', workoutData.workout.name]);
  summarySheet.addRow(['Day Number', workoutData.workout.day_number]);
  summarySheet.addRow(['Created At', new Date(workoutData.workout.created_at).toLocaleDateString()]);
  summarySheet.addRow(['Total Stages', workoutData.stages.length]);

  // Create detailed workout sheet
  const workoutSheet = workbook.addWorksheet('Workout Details');
  workoutSheet.addRow(['Stage', 'Exercise', 'Sets', 'Reps', 'Weight', 'Rest (seconds)', 'Notes']);

  workoutData.stages.forEach((stage: WorkoutStageWithExercises, stageIndex: number) => {
    stage.exercises.forEach((exerciseWithSchemes: ProgrammedExerciseWithSetSchemes) => {
      const exercise = exerciseData.get(exerciseWithSchemes.exercise.exercise_name);
      const weightUnit = weightUnitPreferences.find(
        pref => pref.exercise_name === exerciseWithSchemes.exercise.exercise_name
      )?.preferred_unit || 'lbs';

      exerciseWithSchemes.set_schemes.forEach((setScheme: SetScheme) => {
        workoutSheet.addRow([
          stageIndex + 1,
          exerciseWithSchemes.exercise.exercise_name,
          setScheme.set_number,
          setScheme.target_rep_count || '',
          formatWeightWithUnit(setScheme.target_weight || 0, weightUnit as 'KG' | 'LBS'),
          setScheme.rest_seconds || '',
          ''
        ]);
      });
    });
  });

  // Auto-fit columns
  workoutSheet.columns.forEach(column => {
    column.width = 15;
  });

  // Download the file
  const buffer = await workbook.xlsx.writeBuffer();
  const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${options.filename}.xlsx`;
  link.click();
  window.URL.revokeObjectURL(url);
};

/**
 * Export week workouts to PDF
 */
export const exportWeekToPDF = async (
  weekWorkouts: ProgrammedWorkoutWithStages[],
  exerciseData: Map<string, Exercise>,
  weightUnitPreferences: UserWeightUnitPreference[],
  options: ExportOptions
): Promise<void> => {
  const pdf = new jsPDF('p', 'mm', 'a4');
  
  // Add title
  pdf.setFontSize(18);
  pdf.setFont('helvetica', 'bold');
  pdf.text(options.title, 20, 20);
  
  // Add week info
  pdf.setFontSize(12);
  pdf.setFont('helvetica', 'normal');
  pdf.text(`Total Workouts: ${weekWorkouts.length}`, 20, 30);
  pdf.text(`Total Stages: ${weekWorkouts.reduce((acc, workout) => acc + workout.stages.length, 0)}`, 20, 35);
  
  let currentY = 50;
  
  // Create a table for each workout
  weekWorkouts.forEach((workout, workoutIndex) => {
    if (workoutIndex > 0) {
      pdf.addPage();
      currentY = 20;
    }
    
    // Workout header
    pdf.setFontSize(14);
    pdf.setFont('helvetica', 'bold');
    pdf.text(`Workout ${workoutIndex + 1}: ${workout.workout.name}`, 20, currentY);
    currentY += 10;
    
    // Prepare table data for this workout
    const tableData: string[][] = [];
    const tableHeaders = ['Stage', 'Exercise', 'Sets', 'Reps', 'Weight', 'Rest (s)', 'Notes'];
    
    workout.stages.forEach((stage: WorkoutStageWithExercises, stageIndex: number) => {
      stage.exercises.forEach((exerciseWithSchemes: ProgrammedExerciseWithSetSchemes) => {
        const weightUnit = weightUnitPreferences.find(
          pref => pref.exercise_name === exerciseWithSchemes.exercise.exercise_name
        )?.preferred_unit || 'lbs';

        exerciseWithSchemes.set_schemes.forEach((setScheme: SetScheme) => {
          tableData.push([
            (stageIndex + 1).toString(),
            exerciseWithSchemes.exercise.exercise_name,
            setScheme.set_number.toString(),
            setScheme.target_rep_count?.toString() || '',
            formatWeightWithUnit(setScheme.target_weight || 0, weightUnit as 'KG' | 'LBS'),
            setScheme.rest_seconds?.toString() || '',
            ''
          ]);
        });
      });
    });

    // Add table using autoTable
    (pdf as any).autoTable({
      head: [tableHeaders],
      body: tableData,
      startY: currentY,
      styles: {
        fontSize: 9,
        cellPadding: 2,
      },
      headStyles: {
        fillColor: [66, 139, 202],
        textColor: 255,
        fontStyle: 'bold',
      },
      alternateRowStyles: {
        fillColor: [245, 245, 245],
      },
      columnStyles: {
        0: { cellWidth: 15 }, // Stage
        1: { cellWidth: 50 }, // Exercise
        2: { cellWidth: 15 }, // Sets
        3: { cellWidth: 15 }, // Reps
        4: { cellWidth: 20 }, // Weight
        5: { cellWidth: 15 }, // Rest
        6: { cellWidth: 30 }, // Notes
      },
      margin: { left: 20, right: 20 },
    });
  });

  pdf.save(`${options.filename}.pdf`);
};

/**
 * Export week workouts to XLSX format
 */
export const exportWeekToXLSX = async (
  weekWorkouts: ProgrammedWorkoutWithStages[],
  exerciseData: Map<string, Exercise>,
  weightUnitPreferences: UserWeightUnitPreference[],
  options: ExportOptions
): Promise<void> => {
  const workbook = new ExcelJS.Workbook();
  
  // Create week summary sheet
  const summarySheet = workbook.addWorksheet('Week Summary');
  summarySheet.addRow(['Week Number', options.title]);
  summarySheet.addRow(['Total Workouts', weekWorkouts.length]);
  summarySheet.addRow(['Total Stages', weekWorkouts.reduce((acc, workout) => acc + workout.stages.length, 0)]);

  // Create a sheet for each workout
  weekWorkouts.forEach((workout, workoutIndex) => {
    const workoutSheet = workbook.addWorksheet(`Workout ${workoutIndex + 1}`);
    workoutSheet.addRow(['Stage', 'Exercise', 'Sets', 'Reps', 'Weight', 'Rest (seconds)', 'Notes']);

    workout.stages.forEach((stage: WorkoutStageWithExercises, stageIndex: number) => {
      stage.exercises.forEach((exerciseWithSchemes: ProgrammedExerciseWithSetSchemes) => {
        const weightUnit = weightUnitPreferences.find(
          pref => pref.exercise_name === exerciseWithSchemes.exercise.exercise_name
        )?.preferred_unit || 'lbs';

        exerciseWithSchemes.set_schemes.forEach((setScheme: SetScheme) => {
          workoutSheet.addRow([
            stageIndex + 1,
            exerciseWithSchemes.exercise.exercise_name,
            setScheme.set_number,
            setScheme.target_rep_count || '',
            formatWeightWithUnit(setScheme.target_weight || 0, weightUnit as 'KG' | 'LBS'),
            setScheme.rest_seconds || '',
            ''
          ]);
        });
      });
    });

    // Auto-fit columns
    workoutSheet.columns.forEach(column => {
      column.width = 15;
    });
  });

  // Download the file
  const buffer = await workbook.xlsx.writeBuffer();
  const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${options.filename}.xlsx`;
  link.click();
  window.URL.revokeObjectURL(url);
};

/**
 * Export program workouts to PDF
 */
export const exportProgramToPDF = async (
  programData: ProgramWithWorkouts,
  exerciseData: Map<string, Exercise>,
  weightUnitPreferences: UserWeightUnitPreference[],
  options: ExportOptions
): Promise<void> => {
  const pdf = new jsPDF('p', 'mm', 'a4');
  
  // Add title
  pdf.setFontSize(18);
  pdf.setFont('helvetica', 'bold');
  pdf.text(options.title, 20, 20);
  
  // Add program info
  pdf.setFontSize(12);
  pdf.setFont('helvetica', 'normal');
  pdf.text(`Current Week: ${programData.program.current_week_number}`, 20, 30);
  pdf.text(`Total Workouts: ${programData.workouts.length}`, 20, 35);
  pdf.text(`Total Stages: ${programData.workouts.reduce((acc, workout) => acc + workout.stages.length, 0)}`, 20, 40);
  
  // Group workouts by week
  const workoutsByWeek = new Map<number, ProgrammedWorkoutWithStages[]>();
  programData.workouts.forEach(workout => {
    const weekNumber = Math.ceil(workout.workout.day_number / 7);
    if (!workoutsByWeek.has(weekNumber)) {
      workoutsByWeek.set(weekNumber, []);
    }
    workoutsByWeek.get(weekNumber)!.push(workout);
  });

  let currentY = 50;
  let isFirstWeek = true;
  
  // Create a section for each week
  workoutsByWeek.forEach((weekWorkouts, weekNumber) => {
    if (!isFirstWeek) {
      pdf.addPage();
      currentY = 20;
    }
    isFirstWeek = false;
    
    // Week header
    pdf.setFontSize(16);
    pdf.setFont('helvetica', 'bold');
    pdf.text(`Week ${weekNumber}`, 20, currentY);
    currentY += 10;
    
    // Create a table for each workout in this week
    weekWorkouts.forEach((workout, workoutIndex) => {
      if (workoutIndex > 0) {
        currentY += 10; // Add some space between workouts
      }
      
      // Workout header
      pdf.setFontSize(12);
      pdf.setFont('helvetica', 'bold');
      pdf.text(`Workout ${workoutIndex + 1}: ${workout.workout.name}`, 20, currentY);
      currentY += 8;
      
      // Prepare table data for this workout
      const tableData: string[][] = [];
      const tableHeaders = ['Stage', 'Exercise', 'Sets', 'Reps', 'Weight', 'Rest (s)', 'Notes'];
      
      workout.stages.forEach((stage: WorkoutStageWithExercises, stageIndex: number) => {
        stage.exercises.forEach((exerciseWithSchemes: ProgrammedExerciseWithSetSchemes) => {
          const weightUnit = weightUnitPreferences.find(
            pref => pref.exercise_name === exerciseWithSchemes.exercise.exercise_name
          )?.preferred_unit || 'lbs';

          exerciseWithSchemes.set_schemes.forEach((setScheme: SetScheme) => {
            tableData.push([
              (stageIndex + 1).toString(),
              exerciseWithSchemes.exercise.exercise_name,
              setScheme.set_number.toString(),
              setScheme.target_rep_count?.toString() || '',
              formatWeightWithUnit(setScheme.target_weight || 0, weightUnit as 'KG' | 'LBS'),
              setScheme.rest_seconds?.toString() || '',
              ''
            ]);
          });
        });
      });

      // Add table using autoTable
      (pdf as any).autoTable({
        head: [tableHeaders],
        body: tableData,
        startY: currentY,
        styles: {
          fontSize: 8,
          cellPadding: 2,
        },
        headStyles: {
          fillColor: [66, 139, 202],
          textColor: 255,
          fontStyle: 'bold',
        },
        alternateRowStyles: {
          fillColor: [245, 245, 245],
        },
        columnStyles: {
          0: { cellWidth: 15 }, // Stage
          1: { cellWidth: 50 }, // Exercise
          2: { cellWidth: 15 }, // Sets
          3: { cellWidth: 15 }, // Reps
          4: { cellWidth: 20 }, // Weight
          5: { cellWidth: 15 }, // Rest
          6: { cellWidth: 30 }, // Notes
        },
        margin: { left: 20, right: 20 },
        didDrawPage: (data: any) => {
          currentY = data.cursor.y + 10;
        },
      });
    });
  });

  pdf.save(`${options.filename}.pdf`);
};

/**
 * Export program workouts to XLSX format
 */
export const exportProgramToXLSX = async (
  programData: ProgramWithWorkouts,
  exerciseData: Map<string, Exercise>,
  weightUnitPreferences: UserWeightUnitPreference[],
  options: ExportOptions
): Promise<void> => {
  const workbook = new ExcelJS.Workbook();
  
  // Create program summary sheet
  const summarySheet = workbook.addWorksheet('Program Summary');
  summarySheet.addRow(['Program Name', programData.program.name]);
  summarySheet.addRow(['Current Week', programData.program.current_week_number]);
  summarySheet.addRow(['Total Workouts', programData.workouts.length]);
  summarySheet.addRow(['Total Stages', programData.workouts.reduce((acc, workout) => acc + workout.stages.length, 0)]);

  // Group workouts by week
  const workoutsByWeek = new Map<number, ProgrammedWorkoutWithStages[]>();
  programData.workouts.forEach(workout => {
    const weekNumber = Math.ceil(workout.workout.day_number / 7);
    if (!workoutsByWeek.has(weekNumber)) {
      workoutsByWeek.set(weekNumber, []);
    }
    workoutsByWeek.get(weekNumber)!.push(workout);
  });

  // Create a sheet for each week
  workoutsByWeek.forEach((weekWorkouts, weekNumber) => {
    const weekSheet = workbook.addWorksheet(`Week ${weekNumber}`);
    weekSheet.addRow(['Workout', 'Stage', 'Exercise', 'Sets', 'Reps', 'Weight', 'Rest (seconds)', 'Notes']);

    weekWorkouts.forEach((workout, workoutIndex) => {
      workout.stages.forEach((stage: WorkoutStageWithExercises, stageIndex: number) => {
        stage.exercises.forEach((exerciseWithSchemes: ProgrammedExerciseWithSetSchemes) => {
          const weightUnit = weightUnitPreferences.find(
            pref => pref.exercise_name === exerciseWithSchemes.exercise.exercise_name
          )?.preferred_unit || 'lbs';

          exerciseWithSchemes.set_schemes.forEach((setScheme: SetScheme) => {
            weekSheet.addRow([
              workoutIndex + 1,
              stageIndex + 1,
              exerciseWithSchemes.exercise.exercise_name,
              setScheme.set_number,
              setScheme.target_rep_count || '',
              formatWeightWithUnit(setScheme.target_weight || 0, weightUnit as 'KG' | 'LBS'),
              setScheme.rest_seconds || '',
              ''
            ]);
          });
        });
      });
    });

    // Auto-fit columns
    weekSheet.columns.forEach(column => {
      column.width = 15;
    });
  });

  // Download the file
  const buffer = await workbook.xlsx.writeBuffer();
  const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${options.filename}.xlsx`;
  link.click();
  window.URL.revokeObjectURL(url);
};

/**
 * Print the current page or element
 */
export const printElement = (elementId?: string): void => {
  if (elementId) {
    const element = document.getElementById(elementId);
    if (element) {
      const printWindow = window.open('', '_blank');
      if (printWindow) {
        printWindow.document.write(`
          <html>
            <head>
              <title>Print</title>
              <style>
                body { font-family: Arial, sans-serif; margin: 20px; }
                @media print { body { margin: 0; } }
              </style>
            </head>
            <body>
              ${element.innerHTML}
            </body>
          </html>
        `);
        printWindow.document.close();
        printWindow.print();
      }
    }
  } else {
    window.print();
  }
};
