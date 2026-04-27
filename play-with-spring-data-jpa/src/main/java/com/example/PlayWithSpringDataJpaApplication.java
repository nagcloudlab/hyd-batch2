package com.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.example.entity.Address;
import com.example.entity.Boy;
import com.example.entity.Customer;
import com.example.entity.CustomerGender;
import com.example.entity.Employee;
import com.example.entity.Girl;
import com.example.entity.Project;
import com.example.repository.BoyRepository;
import com.example.repository.CustomerRepository;
import com.example.repository.EmployeeRepository;
import com.example.repository.GirlRepository;
import com.example.repository.ProjectRepository;
import com.example.service.CustomerService;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.repository")
public class PlayWithSpringDataJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlayWithSpringDataJpaApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(
			CustomerRepository customerRepository,
			BoyRepository boyRepository,
			GirlRepository girlRepository,
			EmployeeRepository employeeRepository,
			ProjectRepository projectRepository,
			CustomerService customerService) {
		return args -> {

			// Customer customer = new Customer();
			// customer.setId(3L);
			// customer.setName("Nag");
			// customer.setGender(CustomerGender.MALE);
			// customer.setJoinedDate(new java.util.Date());

			// Address address = new Address();
			// address.setStreet("MG Road");
			// address.setCity("Bangalore");
			// address.setState("KA");
			// address.setCountry("India");

			// Address address2 = new Address();
			// address2.setStreet("Brigade Road");
			// address2.setCity("Bangalore");
			// address2.setState("KA");
			// address2.setCountry("India");

			// customer.setAddresses(java.util.Arrays.asList(address, address2));

			// customerRepository.save(customer);

			// ---- OneToOne Mapping

			// Boy boy = new Boy();
			// boy.setId(123);
			// boy.setName("Ravi");

			// boyRepository.save(boy);

			// Girl girl = new Girl();
			// girl.setId(456);
			// girl.setName("Sita");

			// girlRepository.save(girl);

			// Boy boy = new Boy();
			// boy.setId(123);
			// boy.setName("Ravi");

			// Girl girl = girlRepository.findById(456).get();
			// boy.setGirlfriend(girl);

			// boyRepository.save(boy);

			// Girl girl = girlRepository.findById(456).get();
			// System.out.println("Girlfriend Name: " + girl.getName());
			// // System.out.println("Boyfriend Name: " + girl.getBoyfriend().getName());

			// Project project1 = new Project();
			// project1.setId(1);
			// project1.setName("Project-1");

			// Project project2 = new Project();
			// project2.setId(2);
			// project2.setName("Project-2");

			// Employee employee1 = new Employee();
			// employee1.setId(1);
			// employee1.setName("Nag");

			// Employee employee2 = new Employee();
			// employee2.setId(2);
			// employee2.setName("Ravi");

			// project1.setEmployees(java.util.Arrays.asList(employee1, employee2));
			// project2.setEmployees(java.util.Arrays.asList(employee1, employee2));

			// employee1.setProjects(java.util.Arrays.asList(project1, project2));
			// employee2.setProjects(java.util.Arrays.asList(project1, project2));

			// projectRepository.save(project1);
			// projectRepository.save(project2);

			// ------------

			// Project project = projectRepository.findById(1).get();
			// System.out.println("Project Name: " + project.getName());
			// System.out.println("------------------------------");
			// project.getEmployees().forEach(employee -> {
			// System.out.println("Employee Name: " + employee.getName());
			// });

			customerService.doSomething();

		};
	}

}
