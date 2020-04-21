package com.digidworks.demo.controller.rest;

import com.digidworks.demo.dto.DatasetDto;
import com.digidworks.demo.model.Dataset;
import com.digidworks.demo.model.User;
import com.digidworks.demo.repository.DatasetRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * CRUD controller for datasets.
 */
@RestController
@RequestMapping("/api/datasets")
@Secured("ROLE_USER")
public class DatasetsController {

    @Autowired
    private DatasetRepository datasetRepository;

    /**
     * Get a page of datasets.
     *
     * @param pageable
     * @return
     */
    @GetMapping
    public Page<Dataset> allDatasets(@AuthenticationPrincipal User user, @PageableDefault(size = Integer.MAX_VALUE, value = Integer.MAX_VALUE) Pageable pageable) {
        return datasetRepository.findAllByUser(user, pageable);
    }

    /**
     * Get a dataset by id.
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Dataset singleDataset(@AuthenticationPrincipal User user, @PathVariable String id) {
        return datasetRepository.findByUserAndId(user, id).orElseThrow();
    }

    /**
     * Create a dataset.
     *
     * @return
     */
    @PostMapping(consumes = {"multipart/form-data"})
    public Dataset createDataset(
            @AuthenticationPrincipal User user,
            @RequestPart("dataset") @Valid DatasetDto dto,
            @RequestPart("file") @Valid @NotNull @NotBlank MultipartFile file
    ) throws IOException, CsvException, ExecutionException, InterruptedException {
        return saveDataset(user, dto, null, file);
    }

    /**
     * Update a dataset by id.
     *
     * @param id
     * @param dto
     * @return
     */
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public Dataset updateDataset(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @RequestPart("dataset") @Valid DatasetDto dto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException, CsvException, ExecutionException, InterruptedException {
        if (file != null) {
            return saveDataset(user, dto, id, file);
        }

        Dataset dataset = fetchAndUpdateDataset(user, dto, id);

        datasetRepository.save(dataset);
        return dataset;
    }

    /**
     * Delete dataset by id.
     *
     * @param id
     */
    @DeleteMapping("/{id}")
    public void deleteDataset(@AuthenticationPrincipal User user, @PathVariable String id) {
        datasetRepository.deleteByUserAndId(user, id);
    }

    /**
     * Fetches (or creates) a new Dataset object and makes the basic updates.
     *
     * @param user
     * @param dto
     * @param id
     * @return
     */
    @Async("asyncExecutor")
    protected CompletableFuture<Dataset> fetchAndUpdateDatasetAsync(User user, DatasetDto dto, String id) {
        return CompletableFuture.completedFuture(fetchAndUpdateDataset(user, dto, id));
    }

    /**
     * Reads and parses a CSV uploaded as a MultipartFile.
     *
     * @param file
     * @return
     * @throws IOException
     * @throws CsvException
     */
    @Async("asyncExecutor")
    protected CompletableFuture<List<List<String>>> parseCsv(MultipartFile file) throws IOException, CsvException {
        List<List<String>> result = new ArrayList<>();
        CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()));
        Iterator<String[]> it = reader.iterator();
        Set<Integer> emptyColumns = new HashSet<>();

        String[] firstLine = null;

        if (it.hasNext()) {
            firstLine = it.next();
        }

        if (firstLine != null) {
            List<String> firstLineResult = new ArrayList<>();

            for (int i = 0; i < firstLine.length; i++) {
                if (firstLine[i].isBlank()) {
                    emptyColumns.add(i);
                } else {
                    firstLineResult.add(firstLine[i]);
                }
            }

            result.add(firstLineResult);

            while (it.hasNext()) {
                String[] currLine = it.next();
                List<String> currResult = new ArrayList<>();

                for (int i = 0; i < currLine.length; i++) {
                    if (!emptyColumns.contains(i)) {
                        currResult.add(currLine[i]);
                    }
                }

                result.add(currResult);
            }
        }

        return CompletableFuture.completedFuture(result);
    }

    /**
     * The actual function that runs all the operations needed to create/update a dataset.
     *
     * @param user
     * @param dto
     * @param id
     * @param file
     * @return
     * @throws IOException
     * @throws CsvException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private Dataset saveDataset(User user, DatasetDto dto, String id, MultipartFile file) throws IOException, CsvException, ExecutionException, InterruptedException {
        CompletableFuture<Dataset> datasetFuture = fetchAndUpdateDatasetAsync(user, dto, id);
        CompletableFuture<List<List<String>>> csvLinesFuture = parseCsv(file);

        CompletableFuture.allOf(datasetFuture, csvLinesFuture).join();

        Dataset dataset = datasetFuture.get();

        dataset.setData(csvLinesFuture.get());
        dataset.setRows(dataset.getData().size() - 1);

        datasetRepository.save(dataset);

        return dataset;
    }

    /**
     * Fetches (or creates) a new Dataset object and makes the basic updates.
     *
     * @param user
     * @param dto
     * @param id
     * @return
     */
    private Dataset fetchAndUpdateDataset(User user, DatasetDto dto, String id) {
        Dataset dataset = id != null ? datasetRepository.findByUserAndId(user, id).orElseThrow() : new Dataset();
        dataset.setName(dto.getName());

        if (dataset.getCreatedAt() == null) {
            dataset.setCreatedAt(new Date());
        }
        dataset.setModifiedAt(new Date());
        dataset.setUser(user);

        return dataset;
    }
}
