import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import ExcelJS from 'exceljs';
import type {
  ProgrammedWorkoutWithStages,
  ProgramWithWorkouts,
  UserWeightUnitPreference,
} from '../api/types';
import { capitalizeEachWord } from '../common/utils';

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
 * Sanitize text to prevent PDF formatting issues and ensure proper dash rendering
 */
const sanitizeText = (text: string): string => {
  if (!text) return '';
  // Replace any non-standard dashes with standard ASCII hyphen
  let sanitized = text
    .replace(/[–—]/g, '-') // Replace en-dash and em-dash with standard hyphen
    .replace(/[\u2010-\u2015]/g, '-') // Replace various Unicode dash characters
    .trim();
  
  // Only remove characters that could cause PDF formatting issues
  // Keep all common exercise name characters: letters, numbers, spaces, hyphens, periods, forward slashes, parentheses
  return sanitized.replace(/[^\w\s\-\.\/\(\)]/g, '').trim();
};

/**
 * Modern Congen PDF styling configuration
 */
const PDF_STYLES = {
  fontSize: 9,
  cellPadding: 3,
  headStyles: {
    fillColor: [14, 165, 233], // Congen brand blue #0ea5e9
    textColor: 255,
    fontStyle: 'bold' as const,
    fontSize: 9,
    halign: 'center' as const,
    valign: 'middle' as const,
    cellPadding: 4, // Match section row padding
  },
  // Row styling is handled in didParseCell callback
  // Column styles will be defined in the autoTable call
  margin: { left: 20, right: 20, top: 20, bottom: 20 },
  styles: {
    fontSize: 9,
    cellPadding: 3,
    lineColor: [226, 232, 240], // Light gray border
    lineWidth: 0.5,
    valign: 'middle' as const,
  },
  tableLineColor: [226, 232, 240],
  tableLineWidth: 0.5,
};

/**
 * Modern Congen Excel styling configuration
 */
const EXCEL_STYLES = {
  titleFont: { bold: true, size: 18, color: { argb: 'FF0EA5E9' } }, // Congen brand blue
  headerFont: { bold: true, size: 11, color: { argb: 'FFFFFFFF' } },
  stageFont: { bold: true, size: 10, color: { argb: 'FF0EA5E9' } }, // Bold stage names
  bodyFont: { size: 10 },
  columnWidths: {
    1: 35, // Exercise column
    2: 10, // Sets column
    3: 10, // Reps column
    4: 15, // Weight column
    5: 15, // Rest column
    6: 25, // Notes column
  },
  headerFill: { type: 'pattern' as const, pattern: 'solid' as const, fgColor: { argb: 'FF0EA5E9' } },
  alternateFill: { type: 'pattern' as const, pattern: 'solid' as const, fgColor: { argb: 'FFF0F9FF' } },
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
    tableData.push([sanitizeText(stage.stage.name), '', '', '', '', '']); // Sanitize to ensure proper dash rendering
    
    stage.exercises.forEach((exercise) => {
      const exerciseName = sanitizeText(exercise.exercise.exercise_name); // Sanitize to ensure proper dash rendering
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
        sanitizeText(firstSetScheme.notes || '') // Sanitize notes to ensure proper dash rendering
      ]);
    });
  });
  
  return tableData;
};

/**
 * Add cover page to PDF
 */
const addCoverPage = (
  pdf: jsPDF,
  programData: ProgramWithWorkouts
): void => {
  // Insert cover page at the beginning
  pdf.insertPage(1);
  pdf.setPage(1);
  
  // Set background color (light gray)
  pdf.setFillColor(248, 250, 252);
  pdf.rect(0, 0, 210, 297, 'F');
  
  // Main title - Program name
  pdf.setFontSize(28);
  pdf.setFont('helvetica', 'bold');
  pdf.setTextColor(14, 165, 233); // Congen brand blue
  const programName = programData.program.name;
  const titleWidth = pdf.getTextWidth(programName);
  const titleX = (210 - titleWidth) / 2; // Center horizontally
  pdf.text(programName, titleX, 120);
  
  // Subtitle
  pdf.setFontSize(16);
  pdf.setFont('helvetica', 'normal');
  pdf.setTextColor(71, 85, 105); // Gray text
  const subtitle = 'Training Program Overview';
  const subtitleWidth = pdf.getTextWidth(subtitle);
  const subtitleX = (210 - subtitleWidth) / 2;
  pdf.text(subtitle, subtitleX, 140);
  
  // Program details box
  const boxY = 180;
  const boxHeight = 45; // Reduced height since we removed Program ID
  const boxWidth = 150;
  const boxX = (210 - boxWidth) / 2;
  
  // Box background - using Congen lighter orange
  pdf.setFillColor(255, 237, 213); // secondary.100: '#ffedd5'
  pdf.setDrawColor(226, 232, 240);
  pdf.setLineWidth(0.5);
  pdf.roundedRect(boxX, boxY, boxWidth, boxHeight, 8, 8, 'FD');
  
  // Box content
  pdf.setFontSize(12);
  pdf.setFont('helvetica', 'normal');
  pdf.setTextColor(71, 85, 105);
  
  const details = [
    `Current Week: ${programData.program.current_week_number}`,
    `Created: ${programData.program.created_at.toLocaleDateString()}`,
    `Status: ${programData.program.is_active ? 'Active' : 'Inactive'}`
  ];
  
  details.forEach((detail, index) => {
    const detailY = boxY + 15 + (index * 8);
    pdf.text(detail, boxX + 15, detailY);
  });
  
  // Footer
  pdf.setFontSize(10);
  pdf.setFont('helvetica', 'italic');
  pdf.setTextColor(156, 163, 175);
  const footerText = 'Generated by Congen';
  const footerWidth = pdf.getTextWidth(footerText);
  const footerX = (210 - footerWidth) / 2;
  pdf.text(footerText, footerX, 280);
};

/**
 * Add table of contents page to PDF
 */
const addTableOfContentsPage = (
  pdf: jsPDF,
  tocEntries: Array<{ title: string; page: number; level: number }>
): void => {
  // Insert TOC page at page 2 (after cover page)
  pdf.insertPage(2);
  pdf.setPage(2);
  
  // TOC Header
  pdf.setFontSize(18);
  pdf.setFont('helvetica', 'bold');
  pdf.setTextColor(14, 165, 233); // Congen brand blue
  pdf.text('Table of Contents', 20, 30);
  
  // TOC Entries
  pdf.setFontSize(12);
  pdf.setFont('helvetica', 'normal');
  pdf.setTextColor(0, 0, 0); // Black text
  
  let yPosition = 50;
  const lineHeight = 8;
  
  tocEntries.forEach(entry => {
    const indent = entry.level * 15; // 15mm indent per level
    const title = entry.title;
    const pageNum = (entry.page + 1).toString(); // Add 1 for cover page
    
    // Add title
    pdf.text(title, 20 + indent, yPosition);
    
    // Add dots and page number
    const titleWidth = pdf.getTextWidth(title);
    const availableWidth = 170 - indent - 20; // Total width minus indent and right margin
    const dotsWidth = pdf.getTextWidth('.');
    const pageWidth = pdf.getTextWidth(pageNum);
    const dotsNeeded = Math.floor((availableWidth - titleWidth - pageWidth) / dotsWidth);
    const dots = '.'.repeat(Math.max(1, dotsNeeded));
    
    pdf.text(dots, 20 + indent + titleWidth, yPosition);
    pdf.text(pageNum, 20 + indent + titleWidth + pdf.getTextWidth(dots), yPosition);
    
    // Add clickable link
    const textWidth = pdf.getTextWidth(title + dots + pageNum);
    const textHeight = lineHeight;
    pdf.link(20 + indent, yPosition - textHeight * 0.7, textWidth, textHeight, { pageNumber: entry.page + 1 }); // Add 1 for cover page
    
    yPosition += lineHeight;
  });
};

// Note: PDF outline/bookmarks functionality is not available in standard jsPDF
// The visual table of contents with clickable links provides good navigation

/**
 * Helper function to add a week's content (header + workouts) to the PDF.
 * This function is reused across program, week, and individual workout exports.
 */
const addWeekDetailsToPDF = (
  pdf: jsPDF,
  weekNumber: number,
  weekWorkouts: ProgrammedWorkoutWithStages[],
  startY: number,
  weightUnitPreferences: UserWeightUnitPreference[],
  tocEntries?: Array<{ title: string; page: number; level: number }> // Optional for program export
): number => {
  let currentY = startY;
  const currentPage = pdf.internal.getCurrentPageInfo().pageNumber;

  // Add week header
  pdf.setFontSize(18);
  pdf.setFont('helvetica', 'bold');
  pdf.setTextColor(14, 165, 233); // Congen brand blue
  pdf.text(`Week ${weekNumber}`, 20, currentY);
  currentY += 8; // Space after week header

  // Add TOC entry for week if provided
  if (tocEntries) {
    tocEntries.push({ title: `Week ${weekNumber}`, page: currentPage, level: 1 });
  }

  // Sort workouts by day number
  const sortedWorkouts = weekWorkouts.sort((a, b) => a.workout.day_number - b.workout.day_number);

  // Create a table for each workout in this week
  sortedWorkouts.forEach((workout, index) => {
    // Check if we need a new page for this day
    if (currentY > 200) {
      pdf.addPage();
      currentY = 20;
    }
    
    // Add spacing between days (except for first day)
    if (index > 0) {
      currentY += 8;
    }
    
    // Add TOC entry for day if provided
    if (tocEntries) {
      tocEntries.push({ title: capitalizeEachWord(workout.workout.name), page: currentPage, level: 2 });
    }
    
    // Prepare and add table
    const tableData = prepareWorkoutTableData(workout, weightUnitPreferences);
    currentY = addPDFTable(pdf, tableData, currentY, capitalizeEachWord(workout.workout.name));
  });

  return currentY;
};

/**
 * Add PDF table with modern Congen styling
 */
const addPDFTable = (
  pdf: jsPDF,
  tableData: string[][],
  startY: number,
  title?: string
): number => {
  const tableHeaders = ['Exercise', 'Sets', 'Reps', 'Weight', 'Rest (s)', 'Notes'];
  
  if (title) {
    pdf.setFontSize(14);
    pdf.setFont('helvetica', 'bold');
    pdf.setTextColor(14, 165, 233); // Congen brand blue
    pdf.text(title, 20, startY); // Align with table left margin
    pdf.setTextColor(0, 0, 0); // Reset to black
    startY += 8; // Consistent spacing after title
  }
  
  // Keep original table data for processing
  const processedTableData = tableData;
  
  const result = autoTable(pdf, {
    head: [tableHeaders],
    body: processedTableData,
    startY,
    ...PDF_STYLES,
    tableLineColor: [226, 232, 240],
    tableLineWidth: 0.5,
    showHead: 'everyPage',
    showFoot: 'never',
    // Use full available width
    tableWidth: '100%',
    // Use percentage-based column widths that fill the entire available space
    columnStyles: {
      0: { cellWidth: '42%', halign: 'left' as const }, // Exercise
      1: { cellWidth: '12%', halign: 'center' as const }, // Sets
      2: { cellWidth: '12%', halign: 'center' as const }, // Reps
      3: { cellWidth: '15%', halign: 'center' as const }, // Weight
      4: { cellWidth: '15%', halign: 'center' as const }, // Rest
      5: { cellWidth: '19%', halign: 'left' as const }, // Notes
    },
    didParseCell: (data: any) => {
      // Don't modify header rows - let headStyles handle them
      if (data.section === 'head') {
        return;
      }
      
      // Check if this is a stage name row (has empty cells in other columns)
      const isStageRow = data.row.raw[1] === '' && data.row.raw[2] === '' && 
                        data.row.raw[3] === '' && data.row.raw[4] === '' && data.row.raw[5] === '';
      
      if (isStageRow) {
        if (data.column.index === 0) {
          // Style the stage name cell
          data.cell.styles.fontStyle = 'bold';
          data.cell.styles.fontSize = 10;
          data.cell.styles.textColor = [14, 165, 233]; // Congen brand blue
          data.cell.styles.fillColor = [240, 249, 255]; // Light blue background
          data.cell.styles.cellPadding = 4;
          data.cell.styles.halign = 'left';
          data.cell.styles.valign = 'middle';
        } else {
          // Hide other cells in stage name rows
          data.cell.styles.fillColor = [240, 249, 255];
          data.cell.styles.textColor = [240, 249, 255];
        }
      } else {
        // Exercise rows should have white background and normal font
        data.cell.styles.fillColor = [255, 255, 255]; // White background
        data.cell.styles.textColor = [0, 0, 0]; // Black text
        data.cell.styles.fontStyle = 'normal'; // Ensure normal font weight
        data.cell.styles.fontSize = 9; // Ensure consistent font size
      }
    },
  });
  
  // Return actual final Y position with proper spacing after table
  const finalY = (pdf as any).lastAutoTable?.finalY || (startY + (tableData.length * 6) + 20);
  return finalY + 4; // Reduced spacing after table
};

/**
 * Add Excel table with modern Congen styling
 */
const addExcelTable = (
  sheet: ExcelJS.Worksheet,
  tableData: string[][],
  title?: string
): void => {
  let headerRowIndex = 1;
  
  if (title) {
    const titleRow = sheet.addRow([title]);
    titleRow.font = EXCEL_STYLES.titleFont;
    titleRow.alignment = { horizontal: 'center', vertical: 'middle' };
    titleRow.height = 25; // Consistent row height
    headerRowIndex = 2;
  }
  
  // Add header row
  const headerRow = sheet.addRow(['Exercise', 'Sets', 'Reps', 'Weight', 'Rest (s)', 'Notes']);
  headerRow.font = EXCEL_STYLES.headerFont;
  headerRow.fill = EXCEL_STYLES.headerFill;
  headerRow.alignment = { horizontal: 'center', vertical: 'middle' };
  headerRow.height = 20; // Consistent row height
  
  // Add data rows with styling
  tableData.forEach((row, index) => {
    const dataRow = sheet.addRow(row);
    dataRow.height = 18; // Consistent row height
    
    // Check if this is a stage name row (has empty cells in other columns)
    const isStageRow = row[1] === '' && row[2] === '' && row[3] === '' && row[4] === '' && row[5] === '';
    
    if (isStageRow) {
      // Style stage name rows - merge cells to span full width
      dataRow.font = EXCEL_STYLES.stageFont;
      dataRow.fill = EXCEL_STYLES.alternateFill;
      dataRow.alignment = { horizontal: 'left', vertical: 'middle' };
      
      // Merge cells to span the full width (columns 1-6)
      sheet.mergeCells(index + (title ? 3 : 2), 1, index + (title ? 3 : 2), 6);
    } else {
      // Style regular exercise rows
      dataRow.font = EXCEL_STYLES.bodyFont;
      if (index % 2 === 0) {
        dataRow.fill = EXCEL_STYLES.alternateFill;
      }
      dataRow.alignment = { 
        horizontal: 'left',
        vertical: 'middle'
      };
    }
  });
  
  // Add spacing after table
  sheet.addRow([]);
  sheet.addRow([]);
  
  // Set column widths
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
  
  // Create a single-workout array and use helper function
  const singleWorkoutArray = [workoutData];
  const weekNumber = Math.ceil(workoutData.workout.day_number / 7);
  addWeekDetailsToPDF(pdf, weekNumber, singleWorkoutArray, 20, weightUnitPreferences);
  
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
  
  // Use helper function to add week details (assuming week 1 for single week export)
  const weekNumber = 1;
  addWeekDetailsToPDF(pdf, weekNumber, weekWorkouts, 20, weightUnitPreferences);
  
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
    // Prepare and add table data using the new function
    const tableData = prepareWorkoutTableData(workout, weightUnitPreferences);
    addExcelTable(weekSheet, tableData, `${capitalizeEachWord(workout.workout.name)}`);
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
  let currentY = 20;
  let currentPage = 1;
  
  // Collect TOC entries with page numbers and destinations
  const tocEntries: Array<{ title: string; page: number; level: number }> = [];
  
  // Title is now on cover page, no need to add it here
  
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
  
  // Generate content and track page numbers
  sortedWeeks.forEach((weekNumber) => {
    const weekWorkouts = workoutsByWeek.get(weekNumber)!;
    
    // Check if we need a new page
    if (currentY > 180) {
      pdf.addPage();
      currentY = 20;
      currentPage++;
    }
    
    // Use helper function to add week details
    currentY = addWeekDetailsToPDF(pdf, weekNumber, weekWorkouts, currentY, weightUnitPreferences, tocEntries);
  });
  
  // Add cover page and table of contents
  addCoverPage(pdf, programData);
  addTableOfContentsPage(pdf, tocEntries);
  
  // Note: PDF outline/bookmarks not available in standard jsPDF
  
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
      // Prepare and add table data using the new function
      const tableData = prepareWorkoutTableData(workout, weightUnitPreferences);
      addExcelTable(weekSheet, tableData, `${capitalizeEachWord(workout.workout.name)}`);
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
  
  // Create a single-workout array and use helper function
  const singleWorkoutArray = [workoutData];
  const weekNumber = Math.ceil(workoutData.workout.day_number / 7);
  addWeekDetailsToPDF(pdf, weekNumber, singleWorkoutArray, 20, weightUnitPreferences);
  
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
  
  // Use helper function to add week details (assuming week 1 for single week export)
  const weekNumber = 1;
  addWeekDetailsToPDF(pdf, weekNumber, weekWorkouts, 20, weightUnitPreferences);
  
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
  let currentPage = 1;
  
  // Collect TOC entries with page numbers and destinations
  const tocEntries: Array<{ title: string; page: number; level: number }> = [];
  
  // Title is now on cover page, no need to add it here
  
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
  
  // Generate content and track page numbers
  sortedWeeks.forEach(week => {
    const weekWorkouts = workoutsByWeek.get(week)!;
    
    // Check if we need a new page
    if (currentY > 180) {
      pdf.addPage();
      currentY = 20;
      currentPage++;
    }
    
    // Use helper function to add week details
    currentY = addWeekDetailsToPDF(pdf, week, weekWorkouts, currentY, weightUnitPreferences, tocEntries);
  });
  
  // Add cover page and table of contents
  addCoverPage(pdf, programData);
  addTableOfContentsPage(pdf, tocEntries);
  
  // Note: PDF outline/bookmarks not available in standard jsPDF
  
  await openPrintDialog(pdf);
};