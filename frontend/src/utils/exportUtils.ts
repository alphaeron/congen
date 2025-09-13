import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import ExcelJS from 'exceljs';
import type {
  ProgrammedWorkoutWithStages,
  ProgramWithWorkouts,
  UserWeightUnitPreference,
} from '../api/types';

export interface ExportOptions {
  title: string;
  filename: string;
}

/**
 * Format weight with unit
 */
const formatWeightWithUnit = (weight: number, unit: 'KG' | 'LBS'): string => {
  if (unit === 'LBS') {
    return `${(weight * 2.20462).toFixed(1)} lbs`;
  }
  return `${weight.toFixed(1)} kg`;
};

/**
 * Common PDF styling configuration
 */
const PDF_STYLES = {
  fontSize: 8,
  cellPadding: 2,
  headStyles: {
    fillColor: [41, 128, 185],
    textColor: 255,
    fontStyle: 'bold' as const,
  },
  alternateRowStyles: {
    fillColor: [245, 245, 245],
  },
  columnStyles: {
    0: { cellWidth: 50 }, // Exercise
    1: { cellWidth: 12 }, // Sets
    2: { cellWidth: 12 }, // Reps
    3: { cellWidth: 18 }, // Weight
    4: { cellWidth: 18 }, // Rest
    5: { cellWidth: 30 }, // Notes
  },
  margin: { left: 20, right: 20 },
};

/**
 * Common Excel styling configuration
 */
const EXCEL_STYLES = {
  titleFont: { bold: true, size: 16 },
  columnWidths: {
    1: 30, // Exercise column
    2: 8,  // Sets column
    3: 8,  // Reps column
    4: 12, // Weight column
    5: 12, // Rest column
    6: 20, // Notes column
  },
};

/**
 * Prepare table data for a single workout
 */
const prepareWorkoutTableData = (
  workout: ProgrammedWorkoutWithStages,
  weightUnitPreferences: UserWeightUnitPreference[]
): string[][] => {
  const tableData: string[][] = [];
  
  workout.stages.forEach((stage) => {
    // Add stage name as a full-width row
    tableData.push([stage.stage.name, '', '', '', '', '']);
    
    stage.exercises.forEach((exercise) => {
      const exerciseName = exercise.exercise.exercise_name;
      const totalSets = exercise.set_schemes.length;
      
      // Get the first set scheme for reps, weight, and rest (assuming they're consistent)
      const firstSetScheme = exercise.set_schemes[0];
      const weightUnit = weightUnitPreferences.find(
        pref => pref.user_id === workout.workout.user_id
      )?.preferred_unit || 'KG';
      
      tableData.push([
        exerciseName,
        totalSets.toString(),
        firstSetScheme.target_rep_count?.toString() || '0',
        formatWeightWithUnit(firstSetScheme.target_weight || 0, weightUnit as 'KG' | 'LBS'),
        firstSetScheme.rest_seconds?.toString() || '0',
        firstSetScheme.notes || ''
      ]);
    });
  });
  
  return tableData;
};

/**
 * Add PDF table with common styling
 */
const addPDFTable = (
  pdf: jsPDF,
  tableData: string[][],
  startY: number,
  title?: string
): number => {
  const tableHeaders = ['Exercise', 'Sets', 'Reps', 'Weight', 'Rest (s)', 'Notes'];
  
  if (title) {
    pdf.setFontSize(12);
    pdf.setFont('helvetica', 'bold');
    pdf.text(title, 25, startY);
    startY += 8;
  }
  
  autoTable(pdf, {
    head: [tableHeaders],
    body: tableData,
    startY,
    ...PDF_STYLES,
  });
  
  // Return estimated Y position (fallback for tests)
  return startY + (tableData.length * 8) + 20;
};

/**
 * Add Excel table with common styling
 */
const addExcelTable = (
  sheet: ExcelJS.Worksheet,
  tableData: string[][],
  title?: string
): void => {
  if (title) {
    sheet.addRow([title]);
    sheet.addRow(['Exercise', 'Sets', 'Reps', 'Weight', 'Rest (s)', 'Notes']);
  } else {
    sheet.addRow(['Exercise', 'Sets', 'Reps', 'Weight', 'Rest (s)', 'Notes']);
  }
  
  tableData.forEach(row => sheet.addRow(row));
  
  // Style the sheet
  if (title) {
    sheet.getRow(1).font = EXCEL_STYLES.titleFont;
  }
  Object.entries(EXCEL_STYLES.columnWidths).forEach(([col, width]) => {
    sheet.getColumn(parseInt(col)).width = width;
  });
};

/**
 * Download Excel file
 */
const downloadExcelFile = async (
  workbook: ExcelJS.Workbook,
  filename: string
): Promise<void> => {
  const buffer = await workbook.xlsx.writeBuffer();
  const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${filename}.xlsx`;
  link.click();
  window.URL.revokeObjectURL(url);
};

/**
 * Download PDF file
 */
const downloadPDFFile = (pdf: jsPDF, filename: string): void => {
  pdf.save(`${filename}.pdf`);
};

/**
 * Open print dialog for PDF
 */
const openPrintDialog = async (pdf: jsPDF): Promise<void> => {
  const pdfBlob = pdf.output('blob');
  const pdfUrl = URL.createObjectURL(pdfBlob);
  
  try {
    const printWindow = window.open(pdfUrl, '_blank');
    if (printWindow) {
      printWindow.onload = () => {
        printWindow.print();
        printWindow.close();
      };
    } else {
      throw new Error('Popup blocked. PDF has been downloaded instead.');
    }
  } catch (error) {
    // Fallback: download the PDF
    const link = document.createElement('a');
    link.href = pdfUrl;
    link.download = 'workout.pdf';
    link.click();
    throw new Error('Popup blocked. PDF has been downloaded instead.');
  } finally {
    window.URL.revokeObjectURL(pdfUrl);
  }
};

/**
 * Export a single workout to PDF
 */
export const exportWorkoutToPDF = async (
  workoutData: ProgrammedWorkoutWithStages,
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
  
  // Prepare and add table
  const tableData = prepareWorkoutTableData(workoutData, weightUnitPreferences);
  addPDFTable(pdf, tableData, 50);
  
  downloadPDFFile(pdf, options.filename);
};

/**
 * Export a single workout to XLSX
 */
export const exportWorkoutToXLSX = async (
  workoutData: ProgrammedWorkoutWithStages,
  weightUnitPreferences: UserWeightUnitPreference[],
  options: ExportOptions
): Promise<void> => {
  const workbook = new ExcelJS.Workbook();
  const workoutSheet = workbook.addWorksheet('Workout Details');
  
  // Add title
  workoutSheet.addRow([options.title]);
  workoutSheet.addRow([]); // Empty row for spacing
  
  // Add workout info
  workoutSheet.addRow([`Day: ${workoutData.workout.day_number}`]);
  workoutSheet.addRow([`Created: ${new Date(workoutData.workout.created_at).toLocaleDateString()}`]);
  workoutSheet.addRow([`Total Stages: ${workoutData.stages.length}`]);
  workoutSheet.addRow([]); // Empty row for spacing
  
  // Prepare and add table
  const tableData = prepareWorkoutTableData(workoutData, weightUnitPreferences);
  addExcelTable(workoutSheet, tableData);
  
  await downloadExcelFile(workbook, options.filename);
};

/**
 * Export week workouts to PDF
 */
export const exportWeekToPDF = async (
  weekWorkouts: ProgrammedWorkoutWithStages[],
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
    
    // Prepare and add table
    const tableData = prepareWorkoutTableData(workout, weightUnitPreferences);
    currentY = addPDFTable(pdf, tableData, currentY);
  });
  
  downloadPDFFile(pdf, options.filename);
};

/**
 * Export week workouts to XLSX
 */
export const exportWeekToXLSX = async (
  weekWorkouts: ProgrammedWorkoutWithStages[],
  weightUnitPreferences: UserWeightUnitPreference[],
  options: ExportOptions
): Promise<void> => {
  const workbook = new ExcelJS.Workbook();
  const weekSheet = workbook.addWorksheet('Week Workouts');
  
  // Add title
  weekSheet.addRow([options.title]);
  weekSheet.addRow([]); // Empty row for spacing
  
  // Add week info
  weekSheet.addRow([`Total Workouts: ${weekWorkouts.length}`]);
  weekSheet.addRow([]); // Empty row for spacing
  
  // Sort workouts by day number
  const sortedWorkouts = weekWorkouts.sort((a, b) => a.workout.day_number - b.workout.day_number);
  
  sortedWorkouts.forEach((workout) => {
    // Add day header
    weekSheet.addRow([`Day ${workout.workout.day_number}`]);
    weekSheet.addRow(['Exercise', 'Sets', 'Reps', 'Weight', 'Rest (s)', 'Notes']);
    
    // Prepare and add table data
    const tableData = prepareWorkoutTableData(workout, weightUnitPreferences);
    tableData.forEach(row => weekSheet.addRow(row));
    
    // Add spacing between days
    weekSheet.addRow([]);
    weekSheet.addRow([]);
  });
  
  // Style the sheet
  weekSheet.getRow(1).font = EXCEL_STYLES.titleFont;
  Object.entries(EXCEL_STYLES.columnWidths).forEach(([col, width]) => {
    weekSheet.getColumn(parseInt(col)).width = width;
  });
  
  await downloadExcelFile(workbook, options.filename);
};

/**
 * Export program workouts to PDF
 */
export const exportProgramToPDF = async (
  programData: ProgramWithWorkouts,
  weightUnitPreferences: UserWeightUnitPreference[],
  options: ExportOptions
): Promise<void> => {
  const pdf = new jsPDF('p', 'mm', 'a4');
  
  // Add title
  pdf.setFontSize(18);
  pdf.setFont('helvetica', 'bold');
  pdf.text(options.title, 20, 20);
  
  // Group workouts by week
  const workoutsByWeek = new Map<number, ProgrammedWorkoutWithStages[]>();
  programData.workouts.forEach(workout => {
    const weekNumber = Math.ceil(workout.workout.day_number / 7);
    if (!workoutsByWeek.has(weekNumber)) {
      workoutsByWeek.set(weekNumber, []);
    }
    workoutsByWeek.get(weekNumber)!.push(workout);
  });

  let currentY = 30;
  
  // Sort weeks
  const sortedWeeks = Array.from(workoutsByWeek.keys()).sort((a, b) => a - b);
  
  // Create a section for each week
  sortedWeeks.forEach((weekNumber) => {
    const weekWorkouts = workoutsByWeek.get(weekNumber)!;
    
    // Check if we need a new page
    if (currentY > 180) {
      pdf.addPage();
      currentY = 20;
    }
    
    // Week header
    pdf.setFontSize(14);
    pdf.setFont('helvetica', 'bold');
    pdf.text(`Week ${weekNumber}`, 20, currentY);
    currentY += 8;
    
    // Sort workouts by day number
    const sortedWorkouts = weekWorkouts.sort((a, b) => a.workout.day_number - b.workout.day_number);
    
    // Create a table for each workout in this week
    sortedWorkouts.forEach((workout) => {
      // Check if we need a new page for this day
      if (currentY > 200) {
        pdf.addPage();
        currentY = 20;
      }
      
      // Prepare and add table
      const tableData = prepareWorkoutTableData(workout, weightUnitPreferences);
      currentY = addPDFTable(pdf, tableData, currentY, `Day ${workout.workout.day_number}`);
    });
  });
  
  downloadPDFFile(pdf, options.filename);
};

/**
 * Export program workouts to XLSX
 */
export const exportProgramToXLSX = async (
  programData: ProgramWithWorkouts,
  weightUnitPreferences: UserWeightUnitPreference[],
  options: ExportOptions
): Promise<void> => {
  const workbook = new ExcelJS.Workbook();
  
  // Group workouts by week
  const workoutsByWeek = new Map<number, ProgrammedWorkoutWithStages[]>();
  programData.workouts.forEach(workout => {
    const weekNumber = Math.ceil(workout.workout.day_number / 7);
    if (!workoutsByWeek.has(weekNumber)) {
      workoutsByWeek.set(weekNumber, []);
    }
    workoutsByWeek.get(weekNumber)!.push(workout);
  });

  // Sort weeks
  const sortedWeeks = Array.from(workoutsByWeek.keys()).sort((a, b) => a - b);

  // Create a sheet for each week
  sortedWeeks.forEach((weekNumber) => {
    const weekWorkouts = workoutsByWeek.get(weekNumber)!;
    const weekSheet = workbook.addWorksheet(`Week ${weekNumber}`);
    
    // Add title
    weekSheet.addRow([options.title]);
    weekSheet.addRow([]); // Empty row for spacing
    
    // Sort workouts by day number
    const sortedWorkouts = weekWorkouts.sort((a, b) => a.workout.day_number - b.workout.day_number);
    
    sortedWorkouts.forEach((workout) => {
      // Add day header
      weekSheet.addRow([`Day ${workout.workout.day_number}`]);
      weekSheet.addRow(['Exercise', 'Sets', 'Reps', 'Weight', 'Rest (s)', 'Notes']);
      
      // Prepare and add table data
      const tableData = prepareWorkoutTableData(workout, weightUnitPreferences);
      tableData.forEach(row => weekSheet.addRow(row));
      
      // Add spacing between days
      weekSheet.addRow([]);
      weekSheet.addRow([]);
    });

    // Style the sheet
    weekSheet.getRow(1).font = EXCEL_STYLES.titleFont;
    Object.entries(EXCEL_STYLES.columnWidths).forEach(([col, width]) => {
      weekSheet.getColumn(parseInt(col)).width = width;
    });
  });
  
  await downloadExcelFile(workbook, options.filename);
};

/**
 * Print a workout by generating PDF and opening print dialog
 */
export const printWorkout = async (
  workoutData: ProgrammedWorkoutWithStages,
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
  
  // Prepare and add table
  const tableData = prepareWorkoutTableData(workoutData, weightUnitPreferences);
  addPDFTable(pdf, tableData, 50);
  
  await openPrintDialog(pdf);
};

/**
 * Print week workouts by generating PDF and opening print dialog
 */
export const printWeekWorkouts = async (
  weekWorkouts: ProgrammedWorkoutWithStages[],
  weightUnitPreferences: UserWeightUnitPreference[],
  options: ExportOptions
): Promise<void> => {
  const pdf = new jsPDF('p', 'mm', 'a4');
  let currentY = 20;
  
  // Add title
  pdf.setFontSize(18);
  pdf.setFont('helvetica', 'bold');
  pdf.text(options.title, 20, currentY);
  currentY += 15;
  
  // Add week info
  pdf.setFontSize(12);
  pdf.setFont('helvetica', 'normal');
  pdf.text(`Total Workouts: ${weekWorkouts.length}`, 20, currentY);
  currentY += 10;
  
  weekWorkouts.forEach((workout, workoutIndex) => {
    // Check if we need a new page
    if (currentY > 250) {
      pdf.addPage();
      currentY = 20;
    }
    
    // Add workout header
    pdf.setFontSize(14);
    pdf.setFont('helvetica', 'bold');
    pdf.text(`Workout ${workoutIndex + 1} - Day ${workout.workout.day_number}`, 20, currentY);
    currentY += 10;
    
    // Add workout info
    pdf.setFontSize(10);
    pdf.setFont('helvetica', 'normal');
    pdf.text(`Created: ${new Date(workout.workout.created_at).toLocaleDateString()}`, 20, currentY);
    pdf.text(`Stages: ${workout.stages.length}`, 100, currentY);
    currentY += 8;
    
    // Prepare and add table
    const tableData = prepareWorkoutTableData(workout, weightUnitPreferences);
    currentY = addPDFTable(pdf, tableData, currentY);
  });
  
  await openPrintDialog(pdf);
};

/**
 * Print program workouts by generating PDF and opening print dialog
 */
export const printProgramWorkouts = async (
  programData: ProgramWithWorkouts,
  weightUnitPreferences: UserWeightUnitPreference[],
  options: ExportOptions
): Promise<void> => {
  const pdf = new jsPDF('p', 'mm', 'a4');
  let currentY = 20;
  
  // Add title
  pdf.setFontSize(18);
  pdf.setFont('helvetica', 'bold');
  pdf.text(options.title, 20, currentY);
  currentY += 10;
  
  // Group workouts by week
  const workoutsByWeek = new Map<number, ProgrammedWorkoutWithStages[]>();
  programData.workouts.forEach(workout => {
    const week = Math.ceil(workout.workout.day_number / 7);
    if (!workoutsByWeek.has(week)) {
      workoutsByWeek.set(week, []);
    }
    workoutsByWeek.get(week)!.push(workout);
  });
  
  // Sort weeks
  const sortedWeeks = Array.from(workoutsByWeek.keys()).sort((a, b) => a - b);
  
  sortedWeeks.forEach(week => {
    const weekWorkouts = workoutsByWeek.get(week)!;
    
    // Check if we need a new page
    if (currentY > 180) {
      pdf.addPage();
      currentY = 20;
    }
    
    // Add week header
    pdf.setFontSize(14);
    pdf.setFont('helvetica', 'bold');
    pdf.text(`Week ${week}`, 20, currentY);
    currentY += 8;
    
    // Sort workouts by day number
    const sortedWorkouts = weekWorkouts.sort((a, b) => a.workout.day_number - b.workout.day_number);
    
    // Add each day with its table
    sortedWorkouts.forEach((workout) => {
      // Check if we need a new page for this day
      if (currentY > 200) {
        pdf.addPage();
        currentY = 20;
      }
      
      // Prepare and add table
      const tableData = prepareWorkoutTableData(workout, weightUnitPreferences);
      currentY = addPDFTable(pdf, tableData, currentY, `Day ${workout.workout.day_number}`);
    });
  });
  
  await openPrintDialog(pdf);
};