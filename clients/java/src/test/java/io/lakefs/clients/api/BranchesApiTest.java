/*
 * lakeFS API
 * lakeFS HTTP API
 *
 * The version of the OpenAPI document: 0.1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package io.lakefs.clients.api;

import io.lakefs.clients.api.ApiException;
import io.lakefs.clients.api.model.BranchCreation;
import io.lakefs.clients.api.model.CherryPickCreation;
import io.lakefs.clients.api.model.Commit;
import io.lakefs.clients.api.model.DiffList;
import io.lakefs.clients.api.model.Error;
import io.lakefs.clients.api.model.Ref;
import io.lakefs.clients.api.model.RefList;
import io.lakefs.clients.api.model.ResetCreation;
import io.lakefs.clients.api.model.RevertCreation;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for BranchesApi
 */
@Disabled
public class BranchesApiTest {

    private final BranchesApi api = new BranchesApi();

    /**
     * Cherry-Pick the given reference commit into the given branch
     *
     * @throws ApiException if the Api call fails
     */
    @Test
    public void cherryPickTest() throws ApiException {
        String repository = null;
        String branch = null;
        CherryPickCreation cherryPickCreation = null;
        Commit response = api.cherryPick(repository, branch, cherryPickCreation);
        // TODO: test validations
    }

    /**
     * create branch
     *
     * @throws ApiException if the Api call fails
     */
    @Test
    public void createBranchTest() throws ApiException {
        String repository = null;
        BranchCreation branchCreation = null;
        String response = api.createBranch(repository, branchCreation);
        // TODO: test validations
    }

    /**
     * delete branch
     *
     * @throws ApiException if the Api call fails
     */
    @Test
    public void deleteBranchTest() throws ApiException {
        String repository = null;
        String branch = null;
        api.deleteBranch(repository, branch);
        // TODO: test validations
    }

    /**
     * diff branch
     *
     * @throws ApiException if the Api call fails
     */
    @Test
    public void diffBranchTest() throws ApiException {
        String repository = null;
        String branch = null;
        String after = null;
        Integer amount = null;
        String prefix = null;
        String delimiter = null;
        DiffList response = api.diffBranch(repository, branch, after, amount, prefix, delimiter);
        // TODO: test validations
    }

    /**
     * get branch
     *
     * @throws ApiException if the Api call fails
     */
    @Test
    public void getBranchTest() throws ApiException {
        String repository = null;
        String branch = null;
        Ref response = api.getBranch(repository, branch);
        // TODO: test validations
    }

    /**
     * list branches
     *
     * @throws ApiException if the Api call fails
     */
    @Test
    public void listBranchesTest() throws ApiException {
        String repository = null;
        String prefix = null;
        String after = null;
        Integer amount = null;
        RefList response = api.listBranches(repository, prefix, after, amount);
        // TODO: test validations
    }

    /**
     * reset branch
     *
     * @throws ApiException if the Api call fails
     */
    @Test
    public void resetBranchTest() throws ApiException {
        String repository = null;
        String branch = null;
        ResetCreation resetCreation = null;
        api.resetBranch(repository, branch, resetCreation);
        // TODO: test validations
    }

    /**
     * revert
     *
     * @throws ApiException if the Api call fails
     */
    @Test
    public void revertBranchTest() throws ApiException {
        String repository = null;
        String branch = null;
        RevertCreation revertCreation = null;
        api.revertBranch(repository, branch, revertCreation);
        // TODO: test validations
    }

}
