package com.springtutorial.payroll;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class EmployeeController {

    private final EmployeeRepository repository;

    public EmployeeController(EmployeeRepository repository) {
        this.repository = repository;
    }

    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("/employees")
    CollectionModel<EntityModel<Employee>> all() {

        List<EntityModel<Employee>> employees = repository.findAll().stream()
                .map(employee -> EntityModel.of(employee,
                        linkTo(methodOn(EmployeeController.class)
                                .one(employee.getId()))
                                .withSelfRel(),
                        linkTo(methodOn(EmployeeController.class)
                                .all())
                                .withRel("employees")))
                                .collect(Collectors.toList()
                );

        return CollectionModel.of(employees,
                linkTo(methodOn(EmployeeController.class)
                .all())
                .withSelfRel());
    }
    // end::get-aggregate-root[]

    // POSTs new employee with auto-generated ID
    @PostMapping("/employees")
    Employee newEmployee(@RequestBody Employee newEmployee) {
        return repository.save(newEmployee);
    }

    // Single item
    // GETs an employee with a specific ID
    @GetMapping("/employees/{id}")
    EntityModel<Employee> one(@PathVariable Long id) {

        Employee employee = repository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        return EntityModel.of(employee,
                linkTo(methodOn(EmployeeController.class).one(id)).withSelfRel(),
                linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
    }

    // PUTs a new employee in place of another by ID
    // If employee doesn't exist, POST new employee
    @PutMapping("employees/{id}")
    Employee replaceEmployee(@RequestBody Employee newEmployee,
                             @PathVariable Long id) {

        return repository.findById(id)
                .map(employee -> {
                    employee.setName(newEmployee.getName());
                    employee.setRole(newEmployee.getRole());
                    return repository.save(employee);
                })
                .orElseGet(() -> {
                    newEmployee.setId(id);
                    return repository.save(newEmployee);
                });
    }

    // DELETEs an employee by ID
    @DeleteMapping("/employees/{id}")
    void deleteEmployee(@PathVariable Long id) {
        repository.deleteById(id);
    }

}
