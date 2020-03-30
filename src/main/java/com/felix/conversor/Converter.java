package com.felix.conversor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.felix.classes.People;
import com.felix.classes.TimeRegistry;
import com.felix.controller.ControlQueue;
import com.google.gson.Gson;

public class Converter {

	// #region Variaveis
	Scanner scanner;
	String filePathCSV;
	String filePathJSON;
	ArrayList<TimeRegistry> timeRegistry;
	ControlQueue controlQueue;
	// #endregion

	// Começa o programa
	// Run(args, filePathJSON, tempos, Input, Calculedtempos);

	public Converter() {
		this.timeRegistry = new ArrayList<TimeRegistry>();
		this.filePathJSON = "resources//File.json";
	}

	public void startMenu() {
		// Dá a opção para sair ou continuar
		scanner = new Scanner(System.in);
		System.out.println("Write 'q' to exit or write 'y' to convert an file.");
		String InputKey = scanner.nextLine();

		// Verifica o Input recebido
		if (InputKey.equals("q")) {
			// Finaliza o Programa
			System.out.println("Closing...");
			return;
		} else if (InputKey.equals("y")) {
			// Continua a converter
			convert();
		} else {
			System.out.println("Please write 'q' or 'y'");
			startMenu();
		}

	}

	private void convert() {
		// Input do diretório
		System.out.println("Escreva o diretório do arquivo:\n\nExemplo:\nresources/brasil.csv");
		this.filePathCSV = scanner.next();
		
		this.filePathCSV.replace("/", "//");
		
		// Get path
		Path path = Paths.get(filePathCSV);

		// Verifica se o diretório existe
		if (Files.exists(path)) {

			// Lê o Arquivo
			List<String> fileContent = readFile(path);

			// Cria lista de pessoas
			List<People> peoples = parseToPeoples(fileContent);

			
			// Cria o arquivo JSON
			createJsonFile(peoples);

			// Registra os tempos em um arquivo
			escreverTempos("resources//Times.txt");
			startMenu();
		}
		// Se não existe
		else {
			System.out.println("\nERRO!!!\nO arquivo não existe no diretório selecionado!");
			startMenu();
		}
	}

	private List<People> parseToPeoples(List<String> fileContent) {

		Instant start = Instant.now();

		controlQueue = new ControlQueue(fileContent, 3);
		List<People> listOfPeople = controlQueue.getParsedData();
		
		try {
			//Espera as duas Threads terminaram para continuar a processar
			ControlQueue.th1.join();
			ControlQueue.th2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Registra o tempo que demorou
		timeRegistry.add(new TimeRegistry("parse",Duration.between(start, Instant.now()).toNanos()));

		return listOfPeople;
	}

	private <T> void createJsonFile(List<T> objectList) {
		Instant start = Instant.now();
		// Cria o objeto Gson
		Gson gson = new Gson();

		// Lê o conteudo do arquivo e tranforma em uma String no formato JSON
		String JSON = gson.toJson(objectList);

		// Escreve no arquivo o JSON
		// Adiciona .new ao do arquivo até que não exista nenhum arquivo com o
		// nome igual
		if(Files.exists(Paths.get(filePathJSON))) {
			try {
				Files.delete(Paths.get(filePathJSON));
				System.out.println(filePathJSON + " :: File deleted");
			} catch (IOException e) {e.printStackTrace();}
		}
		
		try {
			BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePathJSON),
					StandardOpenOption.CREATE_NEW);
			writer.write(JSON);
			writer.close();
		} catch (Exception e) {
		}
		
		timeRegistry.add(new TimeRegistry("write",Duration.between(start, Instant.now()).toNanos()));
	}

	private List<String> readFile(Path path) {
		try {
			Instant start = Instant.now();

			// Lê o todas as linhas do arquivo
			List<String> fileContent = Files.readAllLines(path, StandardCharsets.UTF_8);
			timeRegistry.add(new TimeRegistry("read ",Duration.between(start, Instant.now()).toNanos()));

			return fileContent;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void escreverTempos(String filePath) {
		try {
			BufferedWriter writer;
			if (Files.exists(Paths.get(filePath))) {
				Files.delete(Paths.get(filePath));
				System.out.println(filePath + " :: File deleted");
			}

			writer = Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE_NEW);
			String strg = "===================================================\n";
			List<TimeRegistry> temp = controlQueue.getTimeRegistries();
			for (TimeRegistry registry : temp) {
				timeRegistry.add(registry);
			}
			for (int i = 0; i < timeRegistry.size(); i++) {
					strg += timeRegistry.get(i).toString() + "\n";
			}
			strg += "===================================================";
			
			writer.write(strg);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}