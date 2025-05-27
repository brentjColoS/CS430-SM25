
SELECT 
	e.emp_no, last_name, dept_name 
FROM
	employees e, departments d, dept_emp de
WHERE
	e.emp_no = de.emp_no AND de.dept_no = d.dept_no
ORDER BY
	e.emp_no;

SELECT 
	e.emp_no, last_name, dept_name 
FROM
	employees e, departments d, current_dept_emp cde
WHERE
	e.emp_no = cde.emp_no AND cde.dept_no = d.dept_no;

SELECT 
	e.emp_no, last_name, dept_name 
FROM
	employees e, departments d, current_dept_emp cde
WHERE
	e.emp_no = cde.emp_no AND cde.dept_no = d.dept_no
ORDER BY
	e.emp_no;


