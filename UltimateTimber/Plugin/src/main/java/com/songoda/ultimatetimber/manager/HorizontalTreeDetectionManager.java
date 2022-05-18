package com.songoda.ultimatetimber.manager;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.ultimatetimber.UltimateTimber;
import com.songoda.ultimatetimber.tree.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.*;

public class HorizontalTreeDetectionManager extends Manager {

    private final Set<Vector> VALID_TRUNK_OFFSETS;

    private TreeDefinitionManager treeDefinitionManager;
    private PlacedBlockManager placedBlockManager;
    private boolean onlyBreakLogsUpwards;

    public HorizontalTreeDetectionManager(UltimateTimber ultimateTimber) {
        super(ultimateTimber);

        this.VALID_TRUNK_OFFSETS = new HashSet<>();

        // 3x3x3 centered around log
        for (int y = -1; y <= 1; y++)
            for (int x = -1; x <= 1; x++)
                for (int z = -1; z <= 1; z++)
                    this.VALID_TRUNK_OFFSETS.add(new Vector(x, y, z));

    }

    @Override
    public void reload() {
        this.treeDefinitionManager = this.plugin.getTreeDefinitionManager();
        this.placedBlockManager = this.plugin.getPlacedBlockManager();
        this.onlyBreakLogsUpwards = ConfigurationManager.Setting.ONLY_DETECT_LOGS_UPWARDS.getBoolean();
    }

    @Override
    public void disable() {

    }

    /**
     * Detects a tree given an initial starting block
     *
     * @param initialBlock The starting Block of the detection
     * @return A DetectedTree if one was found, otherwise null
     */
    public DetectedTree detectTree(Block initialBlock) {
        TreeDefinitionManager treeDefinitionManager = this.plugin.getTreeDefinitionManager();

        TreeBlock initialTreeBlock = new TreeBlock(initialBlock, TreeBlockType.LOG);
        TreeBlockSet<Block> detectedTreeBlocks = new TreeBlockSet<>(initialTreeBlock);
        Set<TreeDefinition> possibleTreeDefinitions = this.treeDefinitionManager.getTreeDefinitionsForLog(initialBlock);

        if (possibleTreeDefinitions.isEmpty())
            return null;

        // Detect tree trunk
        List<Block> trunkBlocks = new ArrayList<>();
        trunkBlocks.add(initialBlock);
        Block targetBlock = initialBlock;
        while (this.isValidLogType(possibleTreeDefinitions, null, (targetBlock = targetBlock.getRelative(BlockFace.EAST)))) {
            trunkBlocks.add(targetBlock);
            possibleTreeDefinitions.retainAll(this.treeDefinitionManager.narrowTreeDefinition(possibleTreeDefinitions, targetBlock, TreeBlockType.LOG));
        }
        while (this.isValidLogType(possibleTreeDefinitions, null, (targetBlock = targetBlock.getRelative(BlockFace.NORTH)))) {
            trunkBlocks.add(targetBlock);
            possibleTreeDefinitions.retainAll(this.treeDefinitionManager.narrowTreeDefinition(possibleTreeDefinitions, targetBlock, TreeBlockType.LOG));
        }

        if (!this.onlyBreakLogsUpwards) {
            targetBlock = initialBlock;
            while (this.isValidLogType(possibleTreeDefinitions, null, (targetBlock = targetBlock.getRelative(BlockFace.WEST)))) {
                trunkBlocks.add(targetBlock);
                possibleTreeDefinitions.retainAll(this.treeDefinitionManager.narrowTreeDefinition(possibleTreeDefinitions, targetBlock, TreeBlockType.LOG));
            }
            while (this.isValidLogType(possibleTreeDefinitions, null, (targetBlock = targetBlock.getRelative(BlockFace.SOUTH)))) {
                trunkBlocks.add(targetBlock);
                possibleTreeDefinitions.retainAll(this.treeDefinitionManager.narrowTreeDefinition(possibleTreeDefinitions, targetBlock, TreeBlockType.LOG));
            }
        }

        // Lowest blocks at the front of the list
        Collections.reverse(trunkBlocks);

        // Use the first tree definition in the set
        TreeDefinition actualTreeDefinition = possibleTreeDefinitions.iterator().next();

        return new DetectedTree(actualTreeDefinition, detectedTreeBlocks, DetectedTree.Directions.HORIZONTAL);
    }

    /**
     * Checks if a given block is valid for the given TreeDefinitions
     *
     * @param treeDefinitions The Set of TreeDefinitions to compare against
     * @param trunkBlocks     The trunk blocks of the tree for checking the distance
     * @param block           The Block to check
     * @return True if the block is a valid log type, otherwise false
     */
    private boolean isValidLogType(Set<TreeDefinition> treeDefinitions, List<Block> trunkBlocks, Block block) {
        // Check if block is placed
        if (this.placedBlockManager.isBlockPlaced(block))
            return false;

        // Check if it matches the tree definition
        boolean isCorrectType = false;
        for (TreeDefinition treeDefinition : treeDefinitions) {
            for (CompatibleMaterial material : treeDefinition.getLogMaterial()) {
                if (material.equals(CompatibleMaterial.getMaterial(block))) {
                    isCorrectType = true;
                    break;
                }
            }
        }

        if (!isCorrectType)
            return false;

        // Check that it is close enough to the trunk
        if (trunkBlocks == null || trunkBlocks.isEmpty())
            return true;

        Location location = block.getLocation();
        for (TreeDefinition treeDefinition : treeDefinitions) {
            double maxDistance = treeDefinition.getMaxLogDistanceFromTrunk() * treeDefinition.getMaxLogDistanceFromTrunk();
            if (!this.onlyBreakLogsUpwards) // Help detect logs more often if the tree isn't broken at the base
                maxDistance *= 1.5;
            for (Block trunkBlock : trunkBlocks)
                if (location.distanceSquared(trunkBlock.getLocation()) < maxDistance)
                    return true;
        }

        return false;
    }

}
