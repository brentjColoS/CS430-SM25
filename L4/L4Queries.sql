-- Written by Brent Jackson
-- CS430 Lab 4
-- Due 6/1/2025

tee output.txt;

-- 1. Current employees making > $90,000
SELECT e.first_name, e.last_name, s.salary
FROM employees e
JOIN salaries s ON e.emp_no = s.emp_no
WHERE s.to_date = '9999-01-01' AND s.salary > 90000;

-- 2. Current employees with dept # d008 || d009
SELECT e.first_name, e.last_name, d.dept_name
FROM employees e
JOIN dept_emp de ON e.emp_no = de.emp_no
JOIN departments d ON de.dept_no = d.dept_no
WHERE de.to_date = '9999-01-01' AND (de.dept_no = 'd008' OR de.dept_no = 'd009');

-- 3. Current female employees with title "Technique Leader"
SELECT e.first_name, e.last_name, t.title
FROM employees e
JOIN titles t ON e.emp_no = t.emp_no
WHERE e.gender = 'F' AND t.to_date = '9999-01-01' AND t.title = 'Technique Leader';

-- 4. Current employees not titled "Senior Engineer", lowest salaries first
SELECT e.first_name, e.last_name, s.salary
FROM employees e
JOIN salaries s ON e.emp_no = s.emp_no
JOIN titles t ON e.emp_no = t.emp_no
WHERE s.to_date = '9999-01-01' AND t.to_date = '9999-01-01' AND t.title <> 'Senior Engineer'
ORDER BY s.salary ASC;

-- 5. All employees sorted by youngest first
SELECT first_name, last_name, birth_date
FROM employees
ORDER BY birth_date DESC;

-- 6. Current employees who are current department managers
SELECT e.first_name, e.last_name
FROM employees e
JOIN dept_manager dm ON e.emp_no = dm.emp_no
WHERE dm.to_date = '9999-01-01';

-- 7. Employee with the max current salary
SELECT e.first_name, e.last_name, d.dept_name
FROM employees e
JOIN salaries s ON e.emp_no = s.emp_no
JOIN dept_emp de ON e.emp_no = de.emp_no
JOIN departments d ON de.dept_no = d.dept_no
WHERE s.to_date = '9999-01-01' AND de.to_date = '9999-01-01'
AND s.salary = (SELECT MAX(salary) FROM salaries WHERE to_date = '9999-01-01');

notee;
